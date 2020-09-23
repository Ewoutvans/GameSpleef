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
import org.spongepowered.api.util.AABB;

import java.util.*;

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
            this.gameSerialize.campInterval = 7;
            this.gameSerialize.campPlayers = 5;
            this.gameSerialize.campRadius = 2;
            this.gameSerialize.saveInv = true;
            this.gameSerialize.playerLimit = 20;
            this.gameSerialize.winningCommand = Arrays.asList("give %winner% minecraft:diamond 1", "say %winner% got a diamond for winning spleef in arena %game%!");
            this.gameSerialize.winningMinPlayers = 2;
            this.gameSerialize.winningCooldown = 1;
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
                    trySavingFloor(src);
                }
                break;
            case CORNER_FLOOR_2:
                gameSerialize.corner_floor_2 = player.getLocation();
                if (gameSerialize.corner_floor_1 != null) {
                    trySavingFloor(src);
                }
                break;
            case CORNER_AREA_1:
                gameSerialize.corner_area_1 = player.getLocation();
                if (gameSerialize.corner_area_2 != null) {
                    trySavingArea(src);
                }
                break;
            case CORNER_AREA_2:
                gameSerialize.corner_area_2 = player.getLocation();
                if (gameSerialize.corner_area_1 != null) {
                    trySavingArea(src);
                }
                break;
            case SAVE_INV:
                gameSerialize.saveInv = !gameSerialize.saveInv;
                break;
            default:
        }
        showProgress(src);
        return CommandResult.empty();
    }

    private void trySavingFloor(CommandSource src) {
        try {
            // try making the AABB to see if it was setup correctly
            AABB aabb = new AABB(gameSerialize.corner_floor_1.getBlockX(),
                    gameSerialize.corner_floor_1.getBlockY(),
                    gameSerialize.corner_floor_1.getBlockZ(),
                    gameSerialize.corner_floor_2.getBlockX(),
                    gameSerialize.corner_floor_2.getBlockY(),
                    gameSerialize.corner_floor_2.getBlockZ());

            gameSerialize.floors.add(new AABBSerialize(
                    gameSerialize.corner_floor_1.getBlockX(),
                    gameSerialize.corner_floor_1.getBlockY(),
                    gameSerialize.corner_floor_1.getBlockZ(),
                    gameSerialize.corner_floor_2.getBlockX(),
                    gameSerialize.corner_floor_2.getBlockY(),
                    gameSerialize.corner_floor_2.getBlockZ()));
            gameSerialize.corner_floor_1 = null;
            gameSerialize.corner_floor_2 = null;

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                switch (e.getMessage()) {
                    case "The box is degenerate on x":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The floor is not correctly setup, you have selected the same 'X' location for both corners."));
                        break;
                    case "The box is degenerate on y":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The floor is not correctly setup, you have selected the same 'Y' location for both corners. Try going 1 block lower."));
                        break;
                    case "The box is degenerate on z":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The floor is not correctly setup, you have selected the same 'Z' location for both corners."));
                        break;
                    default:
                        src.sendMessage(Text.of(TextColors.DARK_RED, "Unknown problem with making the floor region has occurred."));
                        break;
                }
            }
            gameSerialize.corner_floor_1 = null;
            gameSerialize.corner_floor_2 = null;
        }
    }

    private void trySavingArea(CommandSource src) {
        try {
            // try making the AABB to see if it was setup correctly
            AABB aabb = new AABB(gameSerialize.corner_area_1.getBlockX(),
                    gameSerialize.corner_area_1.getBlockY(),
                    gameSerialize.corner_area_1.getBlockZ(),
                    gameSerialize.corner_area_2.getBlockX(),
                    gameSerialize.corner_area_2.getBlockY(),
                    gameSerialize.corner_area_2.getBlockZ());
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                switch (e.getMessage()) {
                    case "The box is degenerate on x":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The area is not correctly setup, you have selected the same 'X-coordinate' location for both corners."));
                        break;
                    case "The box is degenerate on y":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The area is not correctly setup, you have selected the same 'Y-coordinate' location for both corners. Try going 1 block lower."));
                        break;
                    case "The box is degenerate on z":
                        src.sendMessage(Text.of(TextColors.DARK_RED, "The area is not correctly setup, you have selected the same 'Z-coordinate' location for both corners."));
                        break;
                    default:
                        src.sendMessage(Text.of(TextColors.DARK_RED, "Unknown problem with making the area region has occurred."));
                        break;
                }
            }
            gameSerialize.corner_area_1 = null;
            gameSerialize.corner_area_2 = null;
        }
    }

    private void showProgress(CommandSource src) {
        List<Text> textArray = new ArrayList<>();
        if (gameSerialize == null) {
            textArray.add(Text.builder("No new builder start -click me- to start").onClick(TextActions.suggestCommand("/spleef admin build NAME <name>")).build());
        } else {
            textArray.add(Text.builder("Name: ").color(TextColors.GRAY).append(Text.builder(gameSerialize.name).color(TextColors.GREEN).build()).build());
            textArray.add(Text.builder("Lobby: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.lobby)).onClick(TextActions.runCommand("/spleef admin build LOBBY")).build());
            textArray.add(Text.builder("Can join with items in inv: ").color(TextColors.GRAY).append(Text.of(TextColors.GREEN, gameSerialize.saveInv)).append(Text.of(" (disable this on modded)")).onClick(TextActions.runCommand("/spleef admin build SAVE_INV")).build());
            textArray.add(Text.builder("Spawn: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.spawn)).onClick(TextActions.runCommand("/spleef admin build SPAWN")).build());
            if (gameSerialize.corner_area_1 != null && gameSerialize.corner_area_2 != null) {
                textArray.add(Text.builder("Area: ").color(TextColors.GRAY).append(Text.builder("Okay").color(TextColors.GREEN).build()).build());
            } else {
                textArray.add(Text.builder("Area: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.area)).build());
            }
            textArray.add(Text.builder("   - CORNER_AREA_1").color((gameSerialize.corner_area_1 == null ? TextColors.RED : TextColors.AQUA)).onClick(TextActions.runCommand("/spleef admin build CORNER_AREA_1")).build());
            textArray.add(Text.builder("   - CORNER_AREA_2").color((gameSerialize.corner_area_2 == null ? TextColors.RED : TextColors.AQUA)).onClick(TextActions.runCommand("/spleef admin build CORNER_AREA_2")).build());
            textArray.add(Text.builder("Floors: ").color(TextColors.GRAY).append(colorVariable(gameSerialize.floors)).build());
            textArray.add(Text.builder("   - CORNER_FLOOR_1").color((gameSerialize.corner_floor_1 == null ? TextColors.RED : TextColors.AQUA)).onClick(TextActions.runCommand("/spleef admin build CORNER_FLOOR_1")).build());
            textArray.add(Text.builder("   - CORNER_FLOOR_2").color((gameSerialize.corner_floor_2 == null ? TextColors.RED : TextColors.AQUA)).onClick(TextActions.runCommand("/spleef admin build CORNER_FLOOR_2")).build());
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
            int amount = ((List)object).size();
            return Text.builder(" Amount: " + amount).color((amount == 0 ? TextColors.RED: TextColors.GREEN)).build();
        } else {
            return Text.builder(" Okay").color(TextColors.GREEN).build();
        }
    }
}
