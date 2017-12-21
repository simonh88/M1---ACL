package factory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundFactory {

    private static SoundFactory instance;

    private Clip background;
    private Clip attackSword;
    private Clip attackMonster;

    private SoundFactory(){

        try {
            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File("res/sound/Sword_Swing.wav"));
            attackSword = AudioSystem.getClip();
            attackSword.open(audioInputStream);
        } catch (Exception ex){
            System.out.println("Impossible de charger res/sound/Sword_Swing.wav");
            System.exit(-1);
        }



        try {
            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File("res/sound/Zombie_attack.wav"));
            attackMonster = AudioSystem.getClip();
            attackMonster.open(audioInputStream);

        } catch (Exception ex){
            System.out.println("Impossible de charger res/sound/Zombie_attack.wav");
            System.exit(-1);
        }


    }

    public static SoundFactory instance() {
        if (instance == null) {
            instance = new SoundFactory();
        }

        return instance;
    }

    public void playSound(String fichier){
        // fonctionne qu'avec .wav
        try {


            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File(fichier));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        }catch (Exception ex){
            System.out.println("Impossible de charger " + fichier);
            //System.exit(-1);
        }
    }


    public void playAttackSword(){
        attackSword.setFramePosition(0);
        attackSword.start();
    }

    public void playAttackMonster(){
        attackMonster.setFramePosition(0);
        attackMonster.start();
    }


    public void playBackground(){
        try {
            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File("res/sound/Background_Retro.wav"));
            background = AudioSystem.getClip();
            background.open(audioInputStream);
            background.loop(Clip.LOOP_CONTINUOUSLY);
            background.start();
        }catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Impossible de charger res/sound/Background_Retro.wav");
            System.exit(-1);
        }
    }

    public void stopBackground(){
        background.stop();
    }
}
