package mathgraph.ui;

import mathgraph.core.parser.CompiledExpression;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;

public class AudioSynthesizer {
    private static Thread audioThread;
    private static volatile boolean isPlaying = false;

    public static synchronized void playFunctionSound(CompiledExpression expression, double xMin, double xMax, double durationSec, Runnable onFinish) {
        stopSound();
        if (expression == null) {
            if (onFinish != null) SwingUtilities.invokeLater(onFinish);
            return;
        }

        isPlaying = true;
        audioThread = new Thread(() -> {
            try {
                float sampleRate = 44100f;
                int totalSamples = (int) (sampleRate * durationSec);
                byte[] buffer = new byte[totalSamples];

                double minFreq = 180.0;
                double maxFreq = 950.0;
                double phase = 0.0;

                for (int i = 0; i < totalSamples; i++) {
                    if (!isPlaying) break;

                    double progress = (double) i / totalSamples;
                    double x = xMin + progress * (xMax - xMin);
                    double y = expression.eval(x, 0.0);

                    if (Double.isNaN(y) || Double.isInfinite(y)) {
                        y = 0.0;
                    }

                    // Map y (-5 to 5 approx) to frequency (180 Hz to 950 Hz)
                    double normY = Math.tanh(y / 3.0); // smooth range between -1 and +1
                    double freq = minFreq + (normY + 1.0) / 2.0 * (maxFreq - minFreq);

                    // Sine wave audio synthesis with soft envelope
                    double env = 1.0;
                    int attackSamples = (int) (sampleRate * 0.05);
                    int releaseSamples = (int) (sampleRate * 0.1);
                    if (i < attackSamples) {
                        env = (double) i / attackSamples;
                    } else if (i > totalSamples - releaseSamples) {
                        env = (double) (totalSamples - i) / releaseSamples;
                    }

                    phase += 2.0 * Math.PI * freq / sampleRate;
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI;

                    double wave = Math.sin(phase) * 0.45 * env;
                    buffer[i] = (byte) (wave * 127.0);
                }

                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                if (AudioSystem.isLineSupported(info)) {
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format, buffer.length);
                    line.start();
                    line.write(buffer, 0, buffer.length);
                    line.drain();
                    line.close();
                }
            } catch (Exception e) {
                // Silently handle audio line exceptions if audio output device is busy or unavailable
            } finally {
                isPlaying = false;
                if (onFinish != null) {
                    SwingUtilities.invokeLater(onFinish);
                }
            }
        }, "MathGraphAudioSynth");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    public static synchronized void stopSound() {
        isPlaying = false;
        if (audioThread != null && audioThread.isAlive()) {
            audioThread.interrupt();
        }
    }

    public static boolean isPlaying() {
        return isPlaying;
    }
}
