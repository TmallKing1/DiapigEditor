package top.pigest.queuemanagerdemo.liveroom;

import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.liveroom.data.Gift;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.data.event.*;
import top.pigest.queuemanagerdemo.liveroom.event.*;
import top.pigest.queuemanagerdemo.util.Utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class NarratorService {
    public static NarratorService INSTANCE;
    private CompletableFuture<Void> check;
    private final List<Process> processes = new ArrayList<>();
    private BufferedWriter writer;
    private final List<String> waitForSpeaking = new ArrayList<>();
    private final List<GiftComboSession> giftComboSessions = new ArrayList<>();
    
    public NarratorService() {
        registerHandlers();
    }
    
    public static void enable() {
        INSTANCE = new NarratorService();
    }
    
    public static void disable() {
        INSTANCE.processes.forEach(Process::destroy);
        Utils.onPresent(INSTANCE.check, future -> future.cancel(true));
        INSTANCE.giftComboSessions.forEach(session -> session.timer.cancel());
        INSTANCE = null;
    }

    public void speakNext(String text) {
        try {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            speakNext(text, settings.narratorRate, settings.narratorVolume, settings.narratorVoiceName);
        } catch (Exception ignored) {
        }
    }

    public void startSpeaking() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoExit", "-Command", "-");
        Process process = pb.start();
        processes.add(process);
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "GBK"));

        String command = """
                Add-Type -AssemblyName System.speech
                $speak = New-Object System.Speech.Synthesis.SpeechSynthesizer
                """;
        writer.write(command);
        writer.flush();
        //WRITER.close();
    }

    public void speakNext(String text, double rate, int volume, String voice) throws IOException {
        if (processes.stream().noneMatch(Process::isAlive)) {
            startSpeaking();
        }
        String command = "$speak.Rate = %s\n$speak.Volume = %d\n%s$speak.SpeakAsync('%s')\n".formatted(rate, volume, voice != null ? "$speak.SelectVoice('" + voice.replace("'", "''") + "'); " : "", text.replace("'", "''"));
        writer.write(command);
        writer.flush();
    }

    public void speakIndependent(String text, double rate, int volume, String voice) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("powershell", "-NoExit", "-Command", "-");
        Process process = pb.start();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "GBK"));
        String command = "Add-Type -AssemblyName System.speech\n$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer\n$speak.Rate = %s\n$speak.Volume = %d\n%s$speak.SpeakAsync('%s')\n".formatted(rate, volume, voice != null ? "$speak.SelectVoice('" + voice.replace("'", "''") + "'); " : "", text.replace("'", "''"));
        writer.write(command);
        writer.flush();
    }

    public void stopSpeaking() {
        processes.forEach(process -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            try {
                writer.write("$speak.SpeakAsyncCancelAll()\n");
                writer.flush();
            } catch (IOException ignored) {
            }
            process.destroy();
        });
        processes.clear();
    }

    public static List<Voice> getAvailableVoices() {
        List<Voice> voices = new ArrayList<>();
        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell", "-NoExit", "-Command", "-");
            String command = """
                    Add-Type -AssemblyName System.speech
                    $speak = New-Object System.Speech.Synthesis.SpeechSynthesizer
                    $speak.GetInstalledVoices('zh-CN') | \
                    ForEach-Object { $_.VoiceInfo.Name }
                    exit
                    """;

            Process process = pb.start();
            Writer writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "GBK"));
            writer.write(command);
            writer.flush();
            writer.close();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        voices.add(new Voice(false, line.trim(), null));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return voices;
    }

    public static List<Voice> getRegistrableVoices() {
        List<Voice> voices = new ArrayList<>();
        String windir = System.getenv("windir");
        File file = new File(windir + "\\Speech_OneCore\\Engines\\TTS\\zh-CN");
        String[] list = file.list();
        if (file.isDirectory() && list != null) {
            if (Arrays.stream(list).anyMatch(s -> s.contains("M2052Yaoyao"))) {
                voices.add(new Voice(true, "Microsoft Yaoyao Desktop", Objects.requireNonNull(QueueManager.class.getResource("yy.reg")).getFile()));
            }
            if (Arrays.stream(list).anyMatch(s -> s.contains("M2052Kangkang"))) {
                voices.add(new Voice(true, "Microsoft Kangkang Desktop", Objects.requireNonNull(QueueManager.class.getResource("kk.reg")).getFile()));
            }
        }
        return voices;
    }

    public void registerHandlers() {
        DanmakuEvent.INSTANCE.addHandler(new EventHandler<>("danmaku_narrator", this::handleSingleDanmaku, object -> {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            return settings.acceptedTypes.contains(DanmakuServiceSettings.NarratableElement.DANMAKU);
        }));
        InteractEvent.INSTANCE.addHandler(new EventHandler<>("interact_narrator", this::handleInteract, object -> {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            return settings.acceptedTypes.contains(DanmakuServiceSettings.NarratableElement.ENTER);
        }));
        GiftSendEvent.INSTANCE.addHandler(new EventHandler<>("gift_narrator", this::handleGift, object -> {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            return settings.acceptedTypes.contains(DanmakuServiceSettings.NarratableElement.GIFT);
        }));
        GuardBuyEvent.INSTANCE.addHandler(new EventHandler<>("guard_narrator", this::handleGuard, object -> {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            return settings.acceptedTypes.contains(DanmakuServiceSettings.NarratableElement.GUARD);
        }));
        SuperChatEvent.INSTANCE.addHandler(new EventHandler<>("super_chat", this::handleSuperChat, object -> {
            DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
            return settings.acceptedTypes.contains(DanmakuServiceSettings.NarratableElement.SUPER_CHAT);
        }));
    }

    public void handleSingleDanmaku(Danmaku danmaku) {
            String bar = Settings.getDanmakuServiceSettings().getNarratorText(DanmakuServiceSettings.NarratableElement.DANMAKU)
                    .replace("{user}", danmaku.sender().getUsername())
                    .replace("{comment}", danmaku.content());
            addString(bar);
    }

    public void handleInteract(InteractWord interact) {
        DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
        String bar = settings.getNarratorText(DanmakuServiceSettings.NarratableElement.ENTER)
                .replace("{user}", interact.user().getUsername());
        addString(bar);
    }

    public void handleGift(GiftSend giftSend) {
        giftComboSessions.removeIf(GiftComboSession::isDead);
        DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
        if (settings.giftComboOptimization) {
            Optional<GiftComboSession> optional = giftComboSessions.stream().filter(s -> s.getUser().equals(giftSend.user()) && s.getGift().equalsIgnoreCount(giftSend.gift()) && !s.isDead()).findFirst();
            if (optional.isPresent()) {
                optional.get().onNewGiftReceived(giftSend.gift());
                return;
            } else {
                giftComboSessions.add(new GiftComboSession(giftSend));
            }
        }
        String bar = settings.getNarratorText(DanmakuServiceSettings.NarratableElement.GIFT)
                .replace("{user}", giftSend.user().getUsername())
                .replace("{amount}", String.valueOf(giftSend.gift().getCount()))
                .replace("{gift}", giftSend.gift().getName());
        addString(bar);
    }

    public void handleGuard(GuardBuy guardBuy) {
        DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
        String bar;
        if (settings.multiGuardOptimization) {
            bar = settings.multiGuardText;

        } else {
            bar = settings.getNarratorText(DanmakuServiceSettings.NarratableElement.GUARD);
        }
        bar = bar.replace("{user}", guardBuy.user().getUsername())
                .replace("{amount}", String.valueOf(guardBuy.length()))
                .replace("{guard}", guardBuy.guardType().getName());
        addString(bar);
    }

    public void handleSuperChat(SuperChat superChat) {
        DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
        String bar = settings.getNarratorText(DanmakuServiceSettings.NarratableElement.SUPER_CHAT)
                .replace("{user}", superChat.user().getUsername())
                .replace("{comment}", superChat.content());
        addString(bar);
    }

    public void addString(String s) {
        s = s.replace("\n", "");
        waitForSpeaking.add(s);
        if (check == null || check.isDone()) {
            check = CompletableFuture.runAsync(() -> {
                while (!waitForSpeaking.isEmpty()) {
                    DanmakuServiceSettings settings = Settings.getDanmakuServiceSettings();
                    switch (settings.narratorType) {
                        case DEFAULT -> speakNext(waitForSpeaking.removeFirst());
                        case INTERRUPTED -> {
                            stopSpeaking();
                            speakNext(waitForSpeaking.removeFirst());
                        }
                        case STACKABLE -> {
                            try {
                                speakIndependent(waitForSpeaking.removeFirst(), settings.narratorRate, settings.narratorVolume, settings.narratorVoiceName);
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            });
        }
    }

    public class GiftComboSession {
        private Timer timer;
        private final User user;
        private final Gift gift;
        private long startTime;
        private int count;
        public GiftComboSession(GiftSend giftSend) {
            this.user = giftSend.user();
            this.gift = giftSend.gift();
            this.count = 0;
            this.onNewGiftReceived(gift);
        }

        public User getUser() {
            return user;
        }

        public Gift getGift() {
            return gift;
        }

        public void onNewGiftReceived(Gift gift) {
            this.count += gift.getCount();
            if (this.timer != null) {
                this.timer.cancel();
            }
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                public void run() {
                    if (Settings.getDanmakuServiceSettings().giftComboOptimization && GiftComboSession.this.count > GiftComboSession.this.gift.getCount()) {
                        String bar = Settings.getDanmakuServiceSettings().giftComboEndText
                                .replace("{user}", user.getUsername())
                                .replace("{amount}", String.valueOf(GiftComboSession.this.count))
                                .replace("{gift}", gift.getName());
                        addString(bar);
                    }
                }
            }, 5000);
            this.startTime = System.currentTimeMillis();
        }

        public boolean isDead() {
            return System.currentTimeMillis() - this.startTime > 5000;
        }
    }

    public record Voice(boolean requireRegistration, String name, String registryPath) {
        @Override
        public String toString() {
            return name;
        }
    }
}
