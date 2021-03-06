package game;


import characters.Character;
import characters.Power;
import engine.Cmd;
import environement.DecorType;
import environement.Loot;
import environement.Room;
import factory.SoundFactory;
import utils.Direction;

import java.util.List;
import java.util.Random;

public class Game implements engine.Game {

    /**
     * Pas final car pour la réinitialisation -> new
     */
    private GameSpace gameSpace;
    private GameState gameState;
    private Menu menu;

    private long deltaTime;
    private long deltaTimeAttack;
    private long timeSinceStart;
    private long timeSinceStartAttack;

    //Gestion Chronomètre
    private long startChrono;
    private long currentChrono;

    private int placeFinale;


    private boolean isFinished;

    public Game() {
        this.gameSpace = new GameSpace();
        this.gameState = new GameState();
        this.menu = new Menu(this);
        startChrono = 0;
        currentChrono = 0;
        gameSpace.generateMonsters(gameState);
        isFinished = false;
        generateHero();
        generateBoss();
        deltaTime = 1000;
        deltaTimeAttack = 100;
        timeSinceStart = System.currentTimeMillis();
        SoundFactory.instance().playBackground();
    }

    public GameState getGameState() {
        return gameState;
    }

    /**
     * faire evoluer le jeu suite a une commande
     *
     * @param commande
     */
    @Override
    public void evolve(Cmd commande) {


        /**
         * Si le chrono pas set on le lance
         */
        if (!gameState.isPause() && (startChrono == 0) && gameState.isRunning()) startChrono = System.currentTimeMillis();
        if (!gameState.isPause() && gameState.isRunning()) {
            currentChrono = System.currentTimeMillis();

            if (checkBossHeroSameRoom()) {
                SoundFactory.instance().playHeroBossSameRoom();
            }
        }

        Character hero = gameState.getHero();

        Room currentRoom = currentRoom();


        switch (commande) {
            case LEFT://Gauche
                if (!gameState.isPause()) {
                    int nextX = hero.getPosX() - 1;
                    int nextY = hero.getPosY();
                    if (isValidPosition(nextX, nextY)) {
                        hero.setPosX(hero.getPosX() - 1);
                        hero.setPosY(hero.getPosY());
                    }

                    hero.setLastMove("Q");
                }

                break;
            case DOWN:
                if (!gameState.isPause()){
                    if (isValidPosition(hero.getPosX(), hero.getPosY() + 1)) {
                        hero.setPosX(hero.getPosX());
                        hero.setPosY(hero.getPosY() + 1);
                    }

                    hero.setLastMove("S");
                }


                break;
            case RIGHT:
                if (!gameState.isPause()) {
                    if (isValidPosition(hero.getPosX() + 1, hero.getPosY())) {
                        hero.setPosX(hero.getPosX() + 1);
                        hero.setPosY(hero.getPosY());
                    }

                    hero.setLastMove("D");
                }
                break;
            case UP:
                if (!gameState.isPause()) {
                    if (isValidPosition(hero.getPosX(), hero.getPosY() - 1)) {
                        hero.setPosX(hero.getPosX());
                        hero.setPosY(hero.getPosY() - 1);
                    }

                    hero.setLastMove("Z");
                }
                break;
            case ACTION:
                //SoundFactory.instance().playSound("res/sound/Sword_Swing.wav");
                if (!gameState.isPause()) {
                    Loot loot = heroUse();
                    handleActionLoot(loot);
                }
                break;

            case ATTACK:
                if (!gameState.isPause()) {
                    if (!hero.isOnAttack()) {
                        //SoundFactory.instance().playSound("res/sound/Sword_Swing.wav");
                        SoundFactory.instance().playAttackSword();
                        attackHero();
                    }
                }
                break;
            case RESTART:
                if (gameState.isVictory() || gameState.isLoss()) {
                    restart();
                }
                break;
            case ENTER:
                menu.action();
                break;
            case ARROW_UP:
                menu.setIndiceVersHaut();
                break;
            case ARROW_DOWN:
                menu.setIndiceVersBas();
                break;
            case ECHAP:
                if (menu.isMenuCmd()){
                    menu.setMenuCmd(false);
                } else if (menu.isMenuScore()){
                    menu.setMenuScore(false);
                }

                break;
            case PAUSE:
                gameState.setPause();
                break;
        }

        // Si le joeur est sur un bord il doit changer de map
        if (playerOnBorder()) {
            changeRoom();
        }

        //Fait bouger tous les monstres aléatoirement d'une case toutes les 1sec

        if (!gameState.isPause() && gameState.isRunning() && (System.currentTimeMillis() - timeSinceStart > deltaTime)) {
            attackMonster();
            mooveMonsters();
            timeSinceStart = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - timeSinceStart > deltaTimeAttack) {
            for (Character monster : monsters()) {

                monster.setOnAttack(false);
            }
        }


        //Fait l'animation de l'attaque du hero

        if (System.currentTimeMillis() - timeSinceStartAttack > deltaTimeAttack) {
            hero.setOnAttack(false);
        }

    }


    private void restart() {
        startChrono = 0;
        currentChrono = 0;
        gameState = new GameState();
        gameSpace = new GameSpace();
        gameSpace.generateMonsters(gameState);
        generateHero();
        generateBoss();
        SoundFactory.instance().playBackground();

    }

    private boolean checkBossHeroSameRoom(){
        return gameState.getBoss().getCurrentRoom() == indexCurrentRoom();
    }

    /**
     * Fait bouger tous les monstres vers le héro.
     */
    private void mooveMonsters() {
        Character h = gameState.getHero();


        for (Character monster : monsters()) {

            if (monster.isAlive() && (monster.getCurrentRoom() == indexCurrentRoom())) {
                //System.out.println(" m : "+ monster.getCurrentRoom() + " h : " + indexCurrentRoom());
                //System.out.println("Monster : " + monster.getPosX() + "," + monster.getPosY());
                //System.out.println("Hero : " + h.getPosX() + "," + h.getPosY());


                if (monster.getPosX() > h.getPosX()) {
                    // DROITE
                    if (isValidPosition(monster.getPosX() - 1, monster.getPosY())) {
                        monster.setPosX(monster.getPosX() - 1);
                        monster.setLastMove("Q");
                    }
                }

                if (monster.getPosX() < h.getPosX()) {
                    // GAUCHE
                    if (isValidPosition(monster.getPosX() + 1, monster.getPosY())) {
                        monster.setPosX(monster.getPosX() + 1);
                        monster.setLastMove("D");
                    }
                }

                if (monster.getPosY() > h.getPosY()) {
                    // BAS
                    if (isValidPosition(monster.getPosX(), monster.getPosY() - 1)) {
                        monster.setPosY(monster.getPosY() - 1);
                        monster.setLastMove("Z");
                    }
                }

                if (monster.getPosY() < h.getPosY()) {
                    // HAUT
                    if (isValidPosition(monster.getPosX(), monster.getPosY() + 1)) {
                        monster.setPosY(monster.getPosY() + 1);
                        monster.setLastMove("S");
                    }
                }

            }

        }

    }

    private void generateHero() {
        Random rand = new Random();
        int x = 0;
        int y = 0;
        while (!(isValidPosition(x, y))) {
            x = Math.abs(rand.nextInt()) % (10) + 1;
            y = Math.abs(rand.nextInt()) % (10) + 1;
        }
        gameState.setHero(x, y, 0);

    }

    private void generateBoss() {
        Random rand = new Random();

        //Génération de la salle aléatoire
        int indexRoom = Math.abs(rand.nextInt()) % (gameSpace.sizeRooms()) ;
        Room room = gameSpace.getRoom(indexRoom);

        int x = 0;
        int y = 0;
        while (room.get(x, y).getType() != DecorType.GRASS) {
            x = Math.abs(rand.nextInt()) % (10) + 1;
            y = Math.abs(rand.nextInt()) % (10) + 1;
        }



        gameState.setBoss(x, y, indexRoom);


    }

    private void attackHero() {
        Character hero = gameState.getHero();
        hero.setOnAttack(true);
        timeSinceStartAttack = System.currentTimeMillis();
        int forceAttack = 1;
        //Attaque x2
        if(hero.getPower() == Power.DOUBLEATTACK){
            forceAttack *=2;
        }

        // Attaque vers pots
        heroAttackObject();

        // Attaque vers monstres
        for (Character monster : monsters()) {
            if ((indexCurrentRoom() == monster.getCurrentRoom()) && monster.isAlive()) {

                int distanceX = monster.getPosX() - hero.getPosX();
                int distanceY = monster.getPosY() - hero.getPosY();

                //System.out.println("\n\ndistanceX  : " + distanceX);
                //System.out.println("distanceY  : " + distanceY);
                int range = 1;
                if(hero.getPower() == Power.DOUBLERANGE){
                    range = 2;
                }
                // MONSTRE A GAUCHE
                if(((distanceX == -range || distanceX == -1) && distanceY == 0) &&
                        (hero.getLastMove()=="Q" || hero.getPower() == Power.CIRCLEATTACK)){
                    monster.setHP(monster.getHP() - forceAttack);
                }

                // MONSTRE A DROITE
                if(((distanceX == range || distanceX == 1)&& distanceY == 0) && (hero.getLastMove()=="D" || hero.getPower() == Power.CIRCLEATTACK)){
                    monster.setHP(monster.getHP() - forceAttack);
                }


                // MONSTRE EN HAUT
                if((distanceX == 0  && (distanceY == -range || distanceY == -1) )
                        && (hero.getLastMove()=="Z" || hero.getPower() == Power.CIRCLEATTACK)){
                    monster.setHP(monster.getHP() - forceAttack);
                }

                // MONSTRE EN BAS
                if((distanceX == 0 && (distanceY == range || distanceY == 1) )
                        && (hero.getLastMove()=="S" || hero.getPower() == Power.CIRCLEATTACK)){
                    monster.setHP(monster.getHP() - forceAttack);
                }


                 //Placement de la clé à la mort du boss
                if(monster.getHP() <= 0 && getBoss().equals(monster)){
                    SoundFactory.instance().playKeyDropped();
                    currentRoom().placeGroundLoot(monster.getPosX(), monster.getPosY(), Loot.KEY);
                    // TODO A VOIR SI ON FAIT DROP UN COEUR OU NON
                    //currentRoom().placeGroundLoot(monster.getPosX(), monster.getPosY(), Loot.HEART);

                }
                /*if (distanceX <= 1 && distanceX >= -1 && distanceY <= 1 && distanceY >= -1) {
                    monster.setHP(monster.getHP() - 5);
                }*/
            }

        }

        // Le fait de casser un pot peut tuer le joueur (Cas du loot POISON)
        // On check si le hero est toujours en vie
        if (!hero.isAlive()) {
            gameState.setLoss();
        }
    }

    private void attackMonster() {
        Character hero = gameState.getHero();

        int forceAttack = 1;
        //System.out.println(getBoss().getHP());


        for (Character monster : monsters()) {

            if (monster.isAlive() && (indexCurrentRoom() == monster.getCurrentRoom())) {
                //System.out.println(monster.getHP());


                int distanceX = monster.getPosX() - hero.getPosX();
                int distanceY = monster.getPosY() - hero.getPosY();

                // MONSTRE A GAUCHE
                if(distanceX == -1 && distanceY == 0){
                    SoundFactory.instance().playAttackMonster();
                    hero.setHP(hero.getHP() - forceAttack);
                    monster.setLastMove("D");
                    monster.setOnAttack(true);
                }

                // MONSTRE A DROITE
                if(distanceX == 1 && distanceY == 0){
                    SoundFactory.instance().playAttackMonster();
                    hero.setHP(hero.getHP() - forceAttack);
                    monster.setLastMove("Q");
                    monster.setOnAttack(true);
                }


                // MONSTRE EN HAUT
                if(distanceX == 0 && distanceY == -1){
                    SoundFactory.instance().playAttackMonster();
                    hero.setHP(hero.getHP() - forceAttack);
                    monster.setLastMove("S");
                    monster.setOnAttack(true);
                }

                // MONSTRE EN BAS
                if(distanceX == 0 && distanceY == 1){
                    SoundFactory.instance().playAttackMonster();
                    hero.setHP(hero.getHP() - forceAttack);
                    monster.setLastMove("Z");
                    monster.setOnAttack(true);
                }

                if (!hero.isAlive()) {
                    gameState.setLoss();
                }

            }
        }
    }


    public boolean isValidPosition(int x, int y) {
        // Check monstres
        for (Character monster : monsters()) {
            if (monster.getCurrentRoom() == indexCurrentRoom()) {
                if (x == monster.getPosX() && y == monster.getPosY() && monster.isAlive()) return false;
            }
        }

        // Check Hero
        Character hero = gameState.getHero();
        if (x == hero.getPosX() && y == hero.getPosY()) return false;

        // Check murs
        return gameSpace.isValidPosition(x, y);
    }

    public String toString() {
        return gameSpace.toString(gameState.getHero().getPosX(), gameState.getHero().getPosY());

    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    public Room currentRoom() {
        return gameSpace.currentRoom();
    }

    public int indexCurrentRoom() {
        return gameSpace.indexCurrentRoom();
    }

    public Character getHero() {
        return gameState.getHero();
    }

    public Character getBoss(){
        return gameState.getBoss();
    }


    public List<Character> monsters() {
        return gameState.monsters();
    }

    private boolean isValidposition(int x, int y) {

        return gameSpace.isValidPosition(x, y);
    }

    /**
     * Check si le joueur est sur le bord du plateau, utile pour savoir si on doit changer de map
     *
     * @return boolean
     */
    private boolean playerOnBorder() {
        Character hero = getHero();
        return hero.getPosX() == -1 || hero.getPosY() == -1 || hero.getPosX() == Room.SIZE || hero.getPosY() == Room.SIZE;
    }

    /**
     * On change de room
     */
    private void changeRoom() {
        Character hero = getHero();

        // On détermine vers quelle room le joueur va

        if (hero.getPosX() == -1) {
            // On va à gauche
            gameSpace.goRoom(Direction.LEFT);
            hero.setPosX(Room.SIZE - 1);
        }

        if (hero.getPosX() == Room.SIZE) {
            // On va à droite
            gameSpace.goRoom(Direction.RIGHT);
            hero.setPosX(0);
        }

        if (hero.getPosY() == -1) {
            // On va en haut
            gameSpace.goRoom(Direction.UP);
            hero.setPosY(Room.SIZE - 1);
        }

        if (hero.getPosY() == Room.SIZE) {
            // On va en bas
            gameSpace.goRoom(Direction.BOTTOM);
            hero.setPosY(0);
        }

    }


    public Menu getMenu() {
        return menu;
    }

    private void handleActionLoot(Loot loot) {
        Character hero = getHero();

        switch (loot) {
            case VICTORY:
                SoundFactory.instance().stopBackground();
                gameState.setVictory();
                Data d = new Data();
                placeFinale = d.setScore(getChronoInSec());
                //System.out.println("Votre place : "+d.setScore(getChronoInSec()));
                break;
            case HEART:
                // Ajouter une vie
                hero.setHP(hero.getHP() + 1);
                break;
            case KEY:
                hero.setKey();
                break;
            case DOUBLERANGE:
                hero.setPower(Power.DOUBLERANGE);
                break;
            case CIRCLEATTACK:
                hero.setPower(Power.CIRCLEATTACK);
                break;
            case DOUBLEATTACK:
                hero.setPower(Power.DOUBLEATTACK);
                break;

        }
    }

    private Loot heroUse() {

        Character hero = getHero();
        Loot loot = null;
        Room currentRoom = currentRoom();
        int posX = hero.getPosX();
        int posY = hero.getPosY();

        switch (hero.getLastMove()) {
            case "Z":
                loot = currentRoom.heroUse(posX, posY - 1, hero.hasKey());
                break;
            case "Q":
                loot = currentRoom.heroUse(posX - 1, posY,hero.hasKey());
                break;
            case "S":
                loot = currentRoom.heroUse(posX, posY + 1, hero.hasKey());
                break;
            case "D":
                loot = currentRoom.heroUse(posX + 1, posY, hero.hasKey());
                break;
        }

        if (loot != Loot.NONE) return loot;

        return currentRoom.heroUse(posX, posY, false);
    }

    /**
     * Gere les attaques vers les pots
     * @return
     */
    private void heroAttackObject() {
        Character hero = getHero();
        Room currentRoom = currentRoom();
        int posX = hero.getPosX();
        int posY = hero.getPosY();

        switch (hero.getLastMove()) {
            case "Z":
                currentRoom.heroAttack(posX, posY - 1);
                break;
            case "Q":
                currentRoom.heroAttack(posX - 1, posY);
                break;
            case "S":
                currentRoom.heroAttack(posX, posY + 1);
                break;
            case "D":
                currentRoom.heroAttack(posX + 1, posY);
                break;
        }
    }

    public long getChronoInSec(){
        return (currentChrono - startChrono)/1000;
    }

    public void setDifficulty(String menuDifficulty){
        int difficulty;

        if(menuDifficulty == "Medium") difficulty=2;
        if(menuDifficulty == "Hard") difficulty=3;
        else difficulty=1;

        System.out.println("Difficulty" + difficulty);

        getHero().setHP(getHero().getHP() / difficulty);

        for(Character monster : monsters()){
            if(monster.getId() != 1) monster.setHP(difficulty);
        }


    }

    public int getPlaceFinale(){
        return placeFinale;
    }
}
