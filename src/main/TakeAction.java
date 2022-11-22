package main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.ActionsInput;
import fileio.CardInput;
import fileio.GameInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TakeAction {

    public void start(PlayersDecks playerDeck, GameInput game, ArrayNode output, int noGamesPlayed, Player playerOne, Player playerTwo, MyInteger oneWins, MyInteger twoWins) {
        Table table = Table.getInstance();
        table.clearTable();
        int shuffleSeed = game.getStartGame().getShuffleSeed();
        int startingPlayer = game.getStartGame().getStartingPlayer();
        int indexOne = game.getStartGame().getPlayerOneDeckIdx();
        int indexTwo = game.getStartGame().getPlayerTwoDeckIdx();
        CardInput heroOne = game.getStartGame().getPlayerOneHero();
        CardInput heroTwo = game.getStartGame().getPlayerTwoHero();
        playerOne.makeThatAHero(heroOne);
        playerTwo.makeThatAHero(heroTwo);
        // TODO Put the shuffled in the deck of each player
        deepCopyCards(playerDeck.playerOneDecks, indexOne, playerOne, shuffleSeed);
        deepCopyCards(playerDeck.playerTwoDecks, indexTwo, playerTwo, shuffleSeed);
        if (startingPlayer == 1)
            playerOne.turn = true;
        else
            playerTwo.turn = true;
        ArrayList<ActionsInput> actions = game.getActions();
        if (!playerOne.inPlayDeck.isEmpty())
            playerOne.inHand.add(playerOne.inPlayDeck.remove(0));
        if (!playerTwo.inPlayDeck.isEmpty())
            playerTwo.inHand.add(playerTwo.inPlayDeck.remove(0));
        int roundDone = 0;
        int round = 1;
        playerOne.mana = 1;
        playerTwo.mana = 1;
        for (ActionsInput action : actions) {
            String command = action.getCommand();
            if (roundDone == 2) {
                roundDone %= 2;
                if (round < 9) {
                    playerOne.mana = playerOne.mana + round + 1;
                    playerTwo.mana += round + 1;
                }
                if (round >= 9) {
                    playerOne.mana += 10;
                    playerTwo.mana += 10;
                }
                round++;

            }

            switch (command) {
                case "endPlayerTurn" -> {
                    roundDone++;
                    CommandActionHelper.endTurnForPlayer(playerOne, playerTwo, roundDone);
                }
                case "placeCard" -> {
                    if (playerOne.turn) {
                        CommandActionHelper.placeCardCommand(playerOne, action.getHandIdx(), Table.firstPlayerFrontRow, Table.firstPlayerBackRow, output);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelper.placeCardCommand(playerTwo, action.getHandIdx(), Table.secondPlayerFrontRow, Table.secondPlayerBackRow, output);
                    }
                }
                case "cardUsesAttack" -> {
                    if (playerOne.turn) {
                        //int errorCode = 0;
                        CommandActionHelper.usesAttackCommandHelper(action.getCardAttacker(), action.getCardAttacked(), Table.firstPlayerFrontRow, Table.firstPlayerBackRow, Table.secondPlayerFrontRow, output);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelper.usesAttackCommandHelper(action.getCardAttacker(), action.getCardAttacked(), Table.secondPlayerFrontRow, Table.secondPlayerBackRow, Table.firstPlayerFrontRow, output);
                    }
                }
                case "cardUsesAbility" -> {
                    if (playerOne.turn) {
                        CommandActionHelperModule1.cardUsesAbilityHelper(action.getCardAttacker(), action.getCardAttacked(), output, Table.firstPlayerFrontRow, Table.firstPlayerBackRow, Table.secondPlayerFrontRow, Table.secondPlayerBackRow);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelperModule1.cardUsesAbilityHelper(action.getCardAttacker(), action.getCardAttacked(), output, Table.secondPlayerFrontRow, Table.secondPlayerBackRow, Table.firstPlayerFrontRow, Table.firstPlayerBackRow);
                    }
                }
                case "useAttackHero" -> {
                    if (playerOne.turn) {
                        CommandActionHelperModule1.useAttackHero(playerTwo, Table.secondPlayerFrontRow, 1, oneWins, action.getCardAttacker(), output);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelperModule1.useAttackHero(playerOne, Table.firstPlayerFrontRow, 2, twoWins, action.getCardAttacker(), output);
                    }
                }
                case "useHeroAbility" -> {
                    if (playerOne.turn) {
                        CommandActionHelperModule2.abilityHeroUse(action.getAffectedRow(), playerOne, Table.firstPlayerFrontRow, Table.firstPlayerBackRow, Table.secondPlayerFrontRow, Table.secondPlayerBackRow, output);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelperModule2.abilityHeroUse(action.getAffectedRow(), playerTwo, Table.secondPlayerFrontRow, Table.secondPlayerBackRow, Table.firstPlayerFrontRow, Table.firstPlayerBackRow, output);
                    }
                }
                case "useEnvironmentCard" -> {
                    if (playerOne.turn) {
                        CommandActionHelperModule2.useEnvironmentCard(playerOne, action.getHandIdx(), action.getAffectedRow(), Table.secondPlayerFrontRow, Table.secondPlayerBackRow, output);
                    }
                    if (playerTwo.turn) {
                        CommandActionHelperModule2.useEnvironmentCard(playerTwo, action.getHandIdx(), action.getAffectedRow(), Table.firstPlayerFrontRow, Table.firstPlayerBackRow, output);
                    }
                }
                case "getCardsInHand" -> StatisticInfoHelper.getCardsInHand(action.getPlayerIdx(), playerOne, playerTwo, output);
                case "getPlayerDeck" -> StatisticInfoHelper.getPlayerDeck(action.getPlayerIdx(), playerOne, playerTwo, output);
                case "getCardsOnTable" -> StatisticInfoHelper.getCardsOnTable(output);
                case "getPlayerTurn" -> StatisticInfoHelper.getPlayerTurn(playerOne, output);
                case "getPlayerHero" -> StatisticInfoHelper.getPlayerHero(action.getPlayerIdx(), playerOne, playerTwo, output);
                case "getCardAtPosition" -> StatisticInfoHelper.getCardAtPosition(action.getX(), action.getY(), output);
                case "getPlayerMana" -> StatisticInfoHelper.getPlayerMana(action.getPlayerIdx(), playerOne, playerTwo, output);
                case "getEnvironmentCardsInHand" -> StatisticInfoHelper.getEnvironmentCardsInHand(action.getPlayerIdx(), playerOne, playerTwo, output);
                case "getFrozenCardsOnTable" -> StatisticInfoHelper.getFrozenCardsOnTable(output);
                case "getTotalGamesPlayed" -> StatisticInfoHelper.getTotalGamesPlayed(noGamesPlayed, output);
                case "getPlayerOneWins" -> StatisticInfoHelper.getPlayerWins(1, oneWins, output);
                case "getPlayerTwoWins" -> StatisticInfoHelper.getPlayerWins(2, twoWins, output);
            }
        }
    }

    public void deepCopyCards(Decks choosingDeck, int indexDeck, Player player, int shuffleSeed) {
        List<Cards> auxDeck = choosingDeck.decks.get(indexDeck);
        player.inPlayDeck = new ArrayList<>();
        for (Cards card : auxDeck) {
            if (!card.isNull) {
                if (card.isMinion) {
                    Minion minionCard = (Minion) card;
                    if (minionCard.name.equals("The Cursed One")) {
                        TheCursedOne env = new TheCursedOne(minionCard.mana, minionCard.health, minionCard.attackDamage, minionCard.description, minionCard.name, minionCard.colors);
                        player.inPlayDeck.add(env);
                        continue;
                    }
                    if (minionCard.name.equals("Disciple")) {
                        Disciple env = new Disciple(minionCard.mana, minionCard.health, minionCard.attackDamage, minionCard.description, minionCard.name, minionCard.colors);
                        player.inPlayDeck.add(env);
                        continue;
                    }
                    if (minionCard.name.equals("Miraj")) {
                        Miraj env = new Miraj(minionCard.mana, minionCard.health, minionCard.attackDamage, minionCard.description, minionCard.name, minionCard.colors);
                        player.inPlayDeck.add(env);
                        continue;
                    }
                    if (minionCard.name.equals("The Ripper")) {
                        TheRipper env = new TheRipper(minionCard.mana, minionCard.health, minionCard.attackDamage, minionCard.description, minionCard.name, minionCard.colors);
                        player.inPlayDeck.add(env);
                        continue;
                    }
                    Minion newMini = new Minion(minionCard.mana, minionCard.health, minionCard.attackDamage, minionCard.description, minionCard.name, minionCard.colors);
                    player.inPlayDeck.add(newMini);
                }
                if (card.isEnv) {
                    assert card instanceof Environment;
                    Environment envCard = (Environment) card;
                    if (envCard.name.equals("Winterfell")) {
                        //System.out.println("is one here");
                        Winterfell aux = new Winterfell(envCard.mana, envCard.description, envCard.colors, envCard.name);
                        player.inPlayDeck.add(aux);
                        continue;
                    }
                    if (envCard.name.equals("Firestorm")) {
                        FireStorm aux = new FireStorm(envCard.mana, envCard.description, envCard.colors, envCard.name);
                        player.inPlayDeck.add(aux);
                        continue;
                    }
                    if (envCard.name.equals("Heart Hound")) {
                        HeartHound aux = new HeartHound(envCard.mana, envCard.description, envCard.colors, envCard.name);
                        player.inPlayDeck.add(aux);
                    }
                }
            }
        }
        Random rand = new Random(shuffleSeed);
        Collections.shuffle(player.inPlayDeck, rand);
    }
}
