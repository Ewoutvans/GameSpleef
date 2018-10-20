package cloud.zeroprox.gamespleef;

import cloud.zeroprox.gamespleef.commands.*;
import cloud.zeroprox.gamespleef.commands.admin.BuildCmd;
import cloud.zeroprox.gamespleef.commands.admin.DisableCmd;
import cloud.zeroprox.gamespleef.commands.admin.KickCmd;
import cloud.zeroprox.gamespleef.commands.admin.RemoveCmd;
import cloud.zeroprox.gamespleef.game.GameClassic;
import cloud.zeroprox.gamespleef.game.GameManager;
import cloud.zeroprox.gamespleef.game.IGame;
import cloud.zeroprox.gamespleef.utils.AABBSerialize;
import cloud.zeroprox.gamespleef.utils.GameSerialize;
import cloud.zeroprox.gamespleef.utils.MessageManager;
import cloud.zeroprox.gamespleef.utils.TransformWorldSerializer;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Plugin(id = "gamespleef", name = "GameSpleef", description = "A spleef minigame", url = "https://zeroprox.cloud", authors = {"ewoutvs_", "Alagild"})
public class GameSpleef {

    @Inject
    public Logger logger;
    private static GameSpleef instance;
    private static GameManager gameManager;
    private static MessageManager messageManager;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManagerDefaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    private ConfigurationNode rootNodeDefaultConfig;


    CommandSpec joinCmd = CommandSpec.builder()
            .description(Text.of("Join a game"))
            .arguments(
                    GenericArguments.optional(new GameArgument(Text.of("game")))
            )
            .permission("gamespleef.join")
            .executor(new JoinCmd())
            .build();

    CommandSpec leaveCmd = CommandSpec.builder()
            .description(Text.of("Leave game"))
            .arguments(GenericArguments.flags().flag("f").buildWith(GenericArguments.none()))
            .permission("gamespleef.leave")
            .executor(new LeaveCmd())
            .build();

    CommandSpec adminBuildCmd = CommandSpec.builder()
            .description(Text.of("Build"))
            .arguments(
                    GenericArguments.optional(GenericArguments.enumValue(Text.of("type"), AdminBuildTypes.class)),
                    GenericArguments.optional(GenericArguments.string(Text.of("name")))
            )
            .executor(new BuildCmd())
            .build();

    CommandSpec adminToggleCmd = CommandSpec.builder()
            .description(Text.of("Toggle arena"))
            .arguments(
                    GenericArguments.optional(new GameArgument(Text.of("game")))
            )
            .executor(new DisableCmd())
            .build();

    CommandSpec adminRemoveCmd = CommandSpec.builder()
            .description(Text.of("Remove arena"))
            .arguments(
                    GenericArguments.optional(new GameArgument(Text.of("game")))
            )
            .executor(new RemoveCmd())
            .build();

    CommandSpec adminKickCmd = CommandSpec.builder()
            .description(Text.of("Kick player"))
            .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
            .executor(new KickCmd())
            .build();

    CommandSpec adminCmd = CommandSpec.builder()
            .description(Text.of("Area management"))
            .permission("gamespleef.admin")
            .executor(new AdminCmd())
            .child(adminBuildCmd, "build")
            .child(adminToggleCmd, "toggle")
            .child(adminRemoveCmd, "remove")
            .child(adminKickCmd, "kick")
            .build();

    CommandSpec listCmd = CommandSpec.builder()
            .description(Text.of("Show game list"))
            .executor(new ListCmd())
            .permission("gamespleef.join")
            .build();

    CommandSpec spleefCmd = CommandSpec.builder()
            .description(Text.of("Main command"))
            .child(joinCmd, "join")
            .child(leaveCmd, "leave")
            .child(listCmd, "list")
            .child(adminCmd, "admin")
            .executor(new HelpCmd())
            .build();
    
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, spleefCmd, "gamespleef", "spleef");
        Sponge.getEventManager().registerListeners(this, new Listeners());
        TypeToken<Transform<World>> transformTypeToken = new TypeToken<Transform<World>>() {};
        TypeSerializers.getDefaultSerializers().registerType(transformTypeToken, new TransformWorldSerializer());

        gameManager = new GameManager();
        instance = this;
        configManagerDefaultConfig = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        try {
            rootNodeDefaultConfig = configManagerDefaultConfig.load();
            loadConfig();
            loadMessages();
        } catch(IOException e) {
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public static GameSpleef getInstance() {
        return instance;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    public static MessageManager mM() {
        return messageManager;
    }


    @Listener
    public void onGameReload(GameReloadEvent event) {
        try {
            rootNodeDefaultConfig = configManagerDefaultConfig.load();
            loadConfig();
            loadMessages();
        } catch (IOException e) {
        } catch (ObjectMappingException e) {
        }
    }

    private void loadMessages() {
        messageManager = new MessageManager(privateConfigDir);
    }

    private void loadConfig() throws IOException, ObjectMappingException {
        if (rootNodeDefaultConfig.getNode("areas").isVirtual()) {
            logger.info("Creating configuration");

            rootNodeDefaultConfig.getNode("areas").setValue(new TypeToken<List<GameSerialize>>(){}, Arrays.asList());
            configManagerDefaultConfig.save(rootNodeDefaultConfig);
            loadConfig();
        } else {
            getGameManager().iGames.clear();
            List<GameSerialize> gameSerializeList = rootNodeDefaultConfig.getNode("areas").getList(TypeToken.of(GameSerialize.class));
            for (GameSerialize gameSerialize : gameSerializeList) {
                IGame iGame = null;

                List<AABB> floors = new ArrayList<>();
                for (AABBSerialize aabbSerialize : gameSerialize.floors) {
                    floors.add(aabbSerialize.toAABB());
                }

                if (gameSerialize.gameType == GameType.CLASSIC) {
                    iGame = new GameClassic(gameSerialize.name,
                            gameSerialize.area.toAABB(),
                            floors,
                            gameSerialize.spawn,
                            gameSerialize.lobby,
                            gameSerialize.playerLimit,
                            gameSerialize.campRadius,
                            gameSerialize.campInterval,
                            gameSerialize.campPlayers,
                            gameSerialize.saveInv,
                            gameSerialize.winningCommand,
                            gameSerialize.winningMinPlayers,
                            gameSerialize.winningCooldown
                    );
                }
                getGameManager().iGames.add(iGame);
            }
            logger.info("Loaded: " + getGameManager().iGames.size() + " games");
        }
    }

    public void addArena(GameSerialize gameSerialize) {
        try {
            List<GameSerialize> gameSerializeList = rootNodeDefaultConfig.getNode("areas").getList(TypeToken.of(GameSerialize.class));
            List<GameSerialize> gameList = new ArrayList<>();
            gameList.addAll(gameSerializeList);
            gameList.add(gameSerialize);
            rootNodeDefaultConfig.getNode("areas").setValue(new TypeToken<List<GameSerialize>>(){}, gameList);
            configManagerDefaultConfig.save(rootNodeDefaultConfig);
            loadConfig();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeArena(IGame iGame) {
        try {
            List<GameSerialize> gameSerializeList = rootNodeDefaultConfig.getNode("areas").getList(TypeToken.of(GameSerialize.class));
            List<GameSerialize> gameList = new ArrayList<>();
            gameList.addAll(gameSerializeList);
            gameList.removeIf(gameSerialize -> gameSerialize.name.equalsIgnoreCase(iGame.getName()));
            rootNodeDefaultConfig.getNode("areas").setValue(new TypeToken<List<GameSerialize>>(){}, gameList);
            configManagerDefaultConfig.save(rootNodeDefaultConfig);
            loadConfig();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public enum Mode {
        DISABLED, READY, COUNTDOWN, PLAYING
    }

    public enum GameType {
        CLASSIC
    }

    public enum AdminBuildTypes {
        NAME, LOBBY, SPAWN, CORNER_FLOOR_1, CORNER_FLOOR_2, CORNER_AREA_1, CORNER_AREA_2, SAVE, STOP, TYPE, SAVE_INV
    }

    public class GameArgument extends CommandElement {

        protected GameArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return args.next();
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            List<String> games = new ArrayList<>();
            for (IGame iGame : GameSpleef.getGameManager().iGames) {
                games.add(iGame.getName());
            }
            return games;
        }
    }

}
