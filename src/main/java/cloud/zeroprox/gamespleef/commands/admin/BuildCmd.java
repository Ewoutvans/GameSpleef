package cloud.zeroprox.gamespleef.commands.admin;

import cloud.zeroprox.gamespleef.GameSpleef;
import cloud.zeroprox.gamespleef.utils.AABBSerialize;
import cloud.zeroprox.gamespleef.utils.GameSerialize;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BuildCmd implements CommandExecutor {

    GameSerialize gameSerialize;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "You need to be a player"));
        }
        Player player = (Player)src;
        Optional<GameSpleef.AdminBuildTypes> adminOptional = args.getOne(Text.of("type"));
        if (!adminOptional.isPresent()) {
            showProgress(src);
            return CommandResult.empty();
        }
        GameSpleef.AdminBuildTypes adminType = adminOptional.get();
        if (adminType.equals(GameSpleef.AdminBuildTypes.SAVE)) {

            gameSerialize.area = new AABBSerialize(gameSerialize.corner_area_1.getBlockX(),
                    gameSerialize.corner_area_1.getBlockY(),
                    gameSerialize.corner_area_1.getBlockZ(),
                    gameSerialize.corner_area_2.getBlockX(),
                    gameSerialize.corner_area_2.getBlockY(),
                    gameSerialize.corner_area_2.getBlockZ());

            GameSpleef.getInstance().addArena(gameSerialize);

            src.sendMessage(Text.of("Saved"));
            return CommandResult.success();
        }
        if (adminType.equals(GameSpleef.AdminBuildTypes.NAME)) {
            Optional<String> name = args.getOne(Text.of("name"));
            this.gameSerialize = new GameSerialize();
            this.gameSerialize.gameType = GameSpleef.GameType.CLASSIC;
            this.gameSerialize.name = name.orElse(new Random().nextLong() + "");
            this.gameSerialize.floors = new ArrayList<>();
            showProgress(src);
            return CommandResult.success();
        }
        if (adminType.equals(GameSpleef.AdminBuildTypes.STOP)) {
            this.gameSerialize = null;
            src.sendMessage(Text.of(TextColors.GREEN, "Setup stopped"));
            return CommandResult.success();
        }

        switch (adminType) {
            case LOBBY:
                gameSerialize.lobby = player.getTransform();
                break;
            case SPAWN:
                gameSerialize.spawn = player.getTransform();
                break;
            case CORNER_FLOOR_1:
                gameSerialize.corner_floor_1 = player.getLocation();
                if (gameSerialize.corner_floor_2 != null) {
                    gameSerialize.floors.add(new AABBSerialize(
                            gameSerialize.corner_floor_1.getBlockX(),
                            gameSerialize.corner_floor_1.getBlockY(),
                            gameSerialize.corner_floor_1.getBlockZ(),
                            gameSerialize.corner_floor_2.getBlockX(),
                            gameSerialize.corner_floor_2.getBlockY(),
                            gameSerialize.corner_floor_2.getBlockZ()));
                    gameSerialize.corner_floor_1 = null;
                    gameSerialize.corner_floor_2 = null;
                }
                break;
            case CORNER_FLOOR_2:
                gameSerialize.corner_floor_2 = player.getLocation();
                if (gameSerialize.corner_floor_1 != null) {
                    gameSerialize.floors.add(new AABBSerialize(
                            gameSerialize.corner_floor_1.getBlockX(),
                            gameSerialize.corner_floor_1.getBlockY(),
                            gameSerialize.corner_floor_1.getBlockZ(),
                            gameSerialize.corner_floor_2.getBlockX(),
                            gameSerialize.corner_floor_2.getBlockY(),
                            gameSerialize.corner_floor_2.getBlockZ()));
                    gameSerialize.corner_floor_1 = null;
                    gameSerialize.corner_floor_2 = null;
                }
                break;
            case CORNER_AREA_1:
                gameSerialize.corner_area_1 = player.getLocation();
                break;
            case CORNER_AREA_2:
                gameSerialize.corner_area_2 = player.getLocation();
                break;
            default:
        }
        showProgress(src);
        return CommandResult.empty();
    }

    private void showProgress(CommandSource src) {
        List<Text> textArray = new ArrayList<>();
        if (gameSerialize == null) {
            textArray.add(Text.builder("No new builder start -click me- to start").onClick(TextActions.suggestCommand("/spleef admin build NAME <name>")).build());
        } else {
            textArray.add(Text.builder("Name: ").color(TextColors.GRAY).append(Text.builder(gameSerialize.name).color(TextColors.GREEN).build()).build());
            textArray.add(Text.builder("Lobby: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.lobby)).onClick(TextActions.runCommand("/spleef admin build LOBBY")).build());
            textArray.add(Text.builder("Spawn: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.spawn)).onClick(TextActions.runCommand("/spleef admin build SPAWN")).build());
            if (gameSerialize.corner_area_1 != null && gameSerialize.corner_area_2 != null) {
                textArray.add(Text.builder("Area: ").color(TextColors.GRAY).append(Text.builder("Okay").color(TextColors.GREEN).build()).build());
            } else {
                textArray.add(Text.builder("Area: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.area)).build());
            }
            textArray.add(Text.builder("   - CORNER_AREA_1").color(TextColors.AQUA).onClick(TextActions.runCommand("/spleef admin build CORNER_AREA_1")).build());
            textArray.add(Text.builder("   - CORNER_AREA_2").color(TextColors.AQUA).onClick(TextActions.runCommand("/spleef admin build CORNER_AREA_2")).build());
            textArray.add(Text.builder("Floors: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.floors)).build());
            textArray.add(Text.builder("   - CORNER_FLOOR_1").color(TextColors.AQUA).onClick(TextActions.runCommand("/spleef admin build CORNER_FLOOR_1")).build());
            textArray.add(Text.builder("   - CORNER_FLOOR_2").color(TextColors.AQUA).onClick(TextActions.runCommand("/spleef admin build CORNER_FLOOR_2")).build());
            if (gameSerialize.corner_area_2 != null
                    && gameSerialize.corner_area_1 != null
                    && gameSerialize.floors.size() > 0
                    && gameSerialize.spawn != null
                    && gameSerialize.lobby != null) {
                textArray.add(Text.builder("Save").color(TextColors.AQUA).onClick(TextActions.runCommand("/spleef admin build SAVE")).build());
            }
        }
        PaginationList.builder()
                .title(Text.of("New build arena"))
                .contents(textArray)
                .build()
        .sendTo(src);
    }

    private Text colorVariable(Object object) {
        if (object == null) {
            return Text.builder(" --").color(TextColors.GREEN).build();
        } else if (object instanceof List) {
            return Text.builder(" Amount: " + ((List)object).size()).color(TextColors.RED).build();
        } else {
            return Text.builder(" Okay").color(TextColors.GREEN).build();
        }
    }
}
