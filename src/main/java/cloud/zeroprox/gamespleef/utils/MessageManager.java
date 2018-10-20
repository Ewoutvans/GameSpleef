package cloud.zeroprox.gamespleef.utils;

import static cloud.zeroprox.gamespleef.GameSpleef.getGameManager;
import static org.spongepowered.api.text.TextTemplate.*;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.game.GameClassic;
import cloud.zeroprox.gamespleef.game.IGame;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.AABB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageManager {

    public TextTemplate SPLEEF;
    public TextTemplate SPLEEF_HELP_TITLE;
    public TextTemplate NEED_TO_BE_PLAYER;
    public TextTemplate YOU_ALREADY_PLAYING;
    public TextTemplate GAME_NAME_NOT_FOUND;
    public TextTemplate YOU_ARE_NOT_INGAME;
    public TextTemplate SPLEEF_ARENAS;
    public TextTemplate JOIN_GAME_FULL;
    public TextTemplate GAME_NOT_READY;
    public TextTemplate GAME_ALREADY_STARTED;
    public TextTemplate EMPTY_INV_TO_JOIN;
    public TextTemplate YOU_HAVE_JOINED;
    public TextTemplate YOU_HAVE_BEEN_KNOCKED_BY;
    public TextTemplate PLAYER_WON_BROADCAST;
    public TextTemplate YOU_KNOCKED_OUT;
    public TextTemplate YOU_BROKE;
    public TextTemplate STAT_MOST_KNOCKED;
    public TextTemplate STAT_MOST_BREAKS;
    public TextTemplate KICKED_FOR_CAMPING;
    public TextTemplate POSSIBLE_CAMPING;
    public TextTemplate STARTING;

    private Path messagesConfig;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode rootNode;

    public MessageManager(Path configDir) {
        messagesConfig = Paths.get(new File(configDir.toFile(), "messages.conf").toURI());
        loader = HoconConfigurationLoader.builder().setPath(messagesConfig).build();
        try {
            rootNode = loader.load();
            loadConfig();
        } catch (IOException e) {
        } catch (ObjectMappingException e) {
        }
    }

    private void loadConfig() throws IOException, ObjectMappingException {
        if (rootNode.getNode("messages", "SPLEEF").isVirtual()) {
            GameSpleef.getInstance().logger.info("Creating configuration");

            rootNode.getNode("messages", "SPLEEF").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "["), Text.of(TextColors.RED, "SPLEEF"), Text.of(TextColors.GRAY, "] ")));
            rootNode.getNode("messages", "SPLEEF_HELP_TITLE").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GREEN, "Game Spleef commands")));
            rootNode.getNode("messages", "NEED_TO_BE_PLAYER").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You need to be a player to do this.")));
            rootNode.getNode("messages", "YOU_ALREADY_PLAYING").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You are already in a game.")));
            rootNode.getNode("messages", "GAME_NAME_NOT_FOUND").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "No game found for name "), arg("gamename"), "."));
            rootNode.getNode("messages", "YOU_ARE_NOT_INGAME").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You are not in a game.")));
            rootNode.getNode("messages", "SPLEEF_ARENAS").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GREEN, "Spleef Arena's")));
            rootNode.getNode("messages", "JOIN_GAME_FULL").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You can't join, the game is at its maximum capacity.")));
            rootNode.getNode("messages", "GAME_NOT_READY").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You can't join the game is not ready.")));
            rootNode.getNode("messages", "GAME_ALREADY_STARTED").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You can't join the game is already started.")));
            rootNode.getNode("messages", "EMPTY_INV_TO_JOIN").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.RED, "You need a empty inventory to join.")));
            rootNode.getNode("messages", "YOU_HAVE_JOINED").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GREEN, "You have joined the game")));
            rootNode.getNode("messages", "YOU_HAVE_BEEN_KNOCKED_BY").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "You have been knocked out by "), arg("killer").color(TextColors.RED).build(), "."));
            rootNode.getNode("messages", "PLAYER_WON_BROADCAST").setValue(TypeToken.of(TextTemplate.class), of(arg("winner").color(TextColors.GOLD).build(), " won spleef on arena ", arg("gamename")));
            rootNode.getNode("messages", "YOU_KNOCKED_OUT").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "You knocked out "), arg("amount").color(TextColors.RED).build(), " (", arg("percent"), "%) players"));
            rootNode.getNode("messages", "YOU_BROKE").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "You broke "), arg("amount"), " blocks (", arg("percent"), "%)"));
            rootNode.getNode("messages", "STAT_MOST_KNOCKED").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "Most knockouts by "), arg("player").color(TextColors.RED), " (", arg("amount"), " , ", arg("percent"), "%)"));
            rootNode.getNode("messages", "STAT_MOST_BREAKS").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "Most blocks broken by "), arg("player").color(TextColors.RED), " (", arg("amount"), " , ", arg("percent"), "%)"));
            rootNode.getNode("messages", "KICKED_FOR_CAMPING").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.DARK_RED, "Kicked for camping!")));
            rootNode.getNode("messages", "POSSIBLE_CAMPING").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.DARK_RED, "WARNING: "), Text.of(TextColors.GOLD, "Possible camping detected! Move to keep from being kicked!")));
            rootNode.getNode("messages", "STARTING").setValue(TypeToken.of(TextTemplate.class), of(Text.of(TextColors.GRAY, "Game starting in "), arg("seconds"), " seconds."));

            loader.save(rootNode);
            loadConfig();
        } else {
            SPLEEF = rootNode.getNode("messages", "SPLEEF").getValue(TypeToken.of(TextTemplate.class));
            SPLEEF_HELP_TITLE = rootNode.getNode("messages", "SPLEEF_HELP_TITLE").getValue(TypeToken.of(TextTemplate.class));
            NEED_TO_BE_PLAYER = rootNode.getNode("messages", "NEED_TO_BE_PLAYER").getValue(TypeToken.of(TextTemplate.class));
            YOU_ALREADY_PLAYING = rootNode.getNode("messages", "YOU_ALREADY_PLAYING").getValue(TypeToken.of(TextTemplate.class));
            GAME_NAME_NOT_FOUND = rootNode.getNode("messages", "GAME_NAME_NOT_FOUND").getValue(TypeToken.of(TextTemplate.class));
            YOU_ARE_NOT_INGAME = rootNode.getNode("messages", "YOU_ARE_NOT_INGAME").getValue(TypeToken.of(TextTemplate.class));
            SPLEEF_ARENAS = rootNode.getNode("messages", "SPLEEF_ARENAS").getValue(TypeToken.of(TextTemplate.class));
            JOIN_GAME_FULL = rootNode.getNode("messages", "JOIN_GAME_FULL").getValue(TypeToken.of(TextTemplate.class));
            GAME_NOT_READY = rootNode.getNode("messages", "GAME_NOT_READY").getValue(TypeToken.of(TextTemplate.class));
            GAME_ALREADY_STARTED = rootNode.getNode("messages", "GAME_ALREADY_STARTED").getValue(TypeToken.of(TextTemplate.class));
            EMPTY_INV_TO_JOIN = rootNode.getNode("messages", "EMPTY_INV_TO_JOIN").getValue(TypeToken.of(TextTemplate.class));
            YOU_HAVE_JOINED = rootNode.getNode("messages", "YOU_HAVE_JOINED").getValue(TypeToken.of(TextTemplate.class));
            YOU_HAVE_BEEN_KNOCKED_BY = rootNode.getNode("messages", "YOU_HAVE_BEEN_KNOCKED_BY").getValue(TypeToken.of(TextTemplate.class));
            PLAYER_WON_BROADCAST = rootNode.getNode("messages", "PLAYER_WON_BROADCAST").getValue(TypeToken.of(TextTemplate.class));
            YOU_KNOCKED_OUT = rootNode.getNode("messages", "YOU_KNOCKED_OUT").getValue(TypeToken.of(TextTemplate.class));
            YOU_BROKE = rootNode.getNode("messages", "YOU_BROKE").getValue(TypeToken.of(TextTemplate.class));
            STAT_MOST_KNOCKED = rootNode.getNode("messages", "STAT_MOST_KNOCKED").getValue(TypeToken.of(TextTemplate.class));
            STAT_MOST_BREAKS = rootNode.getNode("messages", "STAT_MOST_BREAKS").getValue(TypeToken.of(TextTemplate.class));
            KICKED_FOR_CAMPING = rootNode.getNode("messages", "KICKED_FOR_CAMPING").getValue(TypeToken.of(TextTemplate.class));
            POSSIBLE_CAMPING = rootNode.getNode("messages", "POSSIBLE_CAMPING").getValue(TypeToken.of(TextTemplate.class));
            STARTING = rootNode.getNode("messages", "STARTING").getValue(TypeToken.of(TextTemplate.class));

            GameSpleef.getInstance().logger.info("Loaded messages");
        }
    }

}
