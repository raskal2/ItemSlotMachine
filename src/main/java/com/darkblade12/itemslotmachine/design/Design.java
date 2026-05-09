package com.darkblade12.itemslotmachine.design;

import com.darkblade12.itemslotmachine.Settings;
import com.darkblade12.itemslotmachine.nameable.Nameable;
import com.darkblade12.itemslotmachine.reference.Direction;
import com.darkblade12.itemslotmachine.reference.ReferenceBlock;
import com.darkblade12.itemslotmachine.reference.ReferenceCuboid;
import com.darkblade12.itemslotmachine.reference.ReferenceItemFrame;
import com.darkblade12.itemslotmachine.util.Cuboid;
import com.darkblade12.itemslotmachine.util.FileUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 3×3グリッド対応のスロットマシンデザイン
 * 9個のItemFrameをサポート（従来の3個から拡張）
 * 
 * グリッド配置:
 *   [0] [1] [2]   (上段)
 *   [3] [4] [5]   (中央行)
 *   [6] [7] [8]   (下段)
 */
public final class Design implements Nameable {
    public static final String DEFAULT_NAME = "default";
    public static final String DEFAULT_FILE = "design_default.json";
    public static final String FILE_EXTENSION = ".json";
    public static final int ITEM_FRAME_COUNT_3x3 = 9;  // 3×3グリッド用
    public static final int ITEM_FRAME_COUNT_3x1 = 3;  // 従来の3リール用（後方互換性）
    
    private String name;
    private Set<ReferenceBlock> blocks;
    private ReferenceItemFrame[] itemFrames;
    private ReferenceBlock sign, slot;
    private ReferenceCuboid region;
    private Direction initialDirection;

    public Design(String name, Set<ReferenceBlock> blocks, ReferenceItemFrame[] itemFrames, ReferenceBlock sign, ReferenceBlock slot,
                  ReferenceCuboid region, Direction initialDirection) {
        this.name = name;
        this.blocks = blocks;
        this.itemFrames = itemFrames;
        this.sign = sign;
        this.slot = slot;
        this.region = region;
        this.initialDirection = initialDirection;
    }

    /**
     * プレイヤーの選択領域からデザインを作成
     * 9個のItemFrameを検出して3×3グリッド対応デザインを作成
     * （従来の3個の場合は後方互換性を保つ）
     */
    public static Design create(Player player, Cuboid cuboid, String name) throws DesignIncompleteException {
        Set<ReferenceBlock> blocks = new HashSet<>();
        ReferenceItemFrame[] itemFrames = new ReferenceItemFrame[ITEM_FRAME_COUNT_3x3];
        ReferenceBlock sign = null, slot = null;
        ReferenceCuboid region = ReferenceCuboid.fromCuboid(player, cuboid);
        Direction direction = Direction.getViewDirection(player);
        int frameIndex = 0;

        for (Block block : cuboid) {
            Material material = block.getType();

            if (block.getState() instanceof Sign && sign == null) {
                sign = ReferenceBlock.fromBukkitBlock(player, block);
            } else if (material == Material.JUKEBOX && slot == null) {
                slot = ReferenceBlock.fromBukkitBlock(player, block);
            } else if (material != Material.AIR) {
                blocks.add(ReferenceBlock.fromBukkitBlock(player, block));
            } else if (frameIndex < ITEM_FRAME_COUNT_3x3) {
                ItemFrame frame = ReferenceItemFrame.findItemFrame(block.getLocation());

                if (frame != null) {
                    itemFrames[frameIndex++] = ReferenceItemFrame.fromBukkitItemFrame(player, frame);
                }
            }
        }

        // 最低3個のItemFrameが必要（従来の3リール対応）
        if (frameIndex < ITEM_FRAME_COUNT_3x1) {
            int missingFrames = ITEM_FRAME_COUNT_3x1 - frameIndex;
            String ending = missingFrames == 1 ? "" : "s";
            throw new DesignIncompleteException("The design is missing %d item frame%s. (Minimum: 3, Recommended: 9)", missingFrames, ending);
        } else if (sign == null) {
            throw new DesignIncompleteException("The design is missing a pot sign.");
        } else if (slot == null) {
            throw new DesignIncompleteException("The design is missing a slot (jukebox).");
        }

        // 3個の場合、配列を3要素に縮小（後方互換性）
        if (frameIndex == ITEM_FRAME_COUNT_3x1) {
            ReferenceItemFrame[] compactFrames = new ReferenceItemFrame[ITEM_FRAME_COUNT_3x1];
            System.arraycopy(itemFrames, 0, compactFrames, 0, ITEM_FRAME_COUNT_3x1);
            itemFrames = compactFrames;
        }

        return new Design(name, blocks, itemFrames, sign, slot, region, direction);
    }

    public static Design fromFile(File file) throws IOException, JsonParseException {
        return FileUtils.readJson(file, Design.class);
    }

    public static Design fromFile(String path) throws IOException, JsonParseException {
        return FileUtils.readJson(new File(path), Design.class);
    }

    public static void convert(JsonObject design) throws DesignIncompleteException {
        JsonObject region = design.getAsJsonObject("region");
        JsonElement firstVertex = region.remove("firstVertice");
        JsonElement secondVertex = region.remove("secondVertice");
        if (firstVertex.isJsonNull() || secondVertex.isJsonNull()) {
            throw new DesignIncompleteException("The design is missing region vertices.");
        }

        region.add("firstVertex", firstVertex);
        region.add("secondVertex", secondVertex);
    }

    /**
     * 3リール用：最初と最後のItemFrameを反転
     * （3×3グリッドの場合は効果がない可能性あり）
     */
    public void invertItemFrames() {
        if (itemFrames.length >= 3) {
            ReferenceItemFrame temp = itemFrames[0];
            itemFrames[0] = itemFrames[itemFrames.length - 1];
            itemFrames[itemFrames.length - 1] = temp;
        }
    }

    public void build(Location viewPoint, Direction viewDirection, Settings settings) throws DesignBuildException {
        Cuboid cuboid = region.toCuboid(viewPoint, viewDirection);
        if (settings.isSpaceCheckEnabled()) {
            List<Material> ignoredTypes = settings.getSpaceCheckIgnoredTypes();
            for (Block block : cuboid) {
                Material material = block.getType();
                if (material != Material.AIR && !ignoredTypes.contains(material)) {
                    throw new DesignBuildException("There is not enough space for this design.");
                }
            }
        }

        try {
            for (ReferenceBlock refBlock : blocks) {
                refBlock.place(viewPoint, viewDirection);
            }

            sign.place(viewPoint, viewDirection);
            slot.place(viewPoint, viewDirection);

            for (ReferenceItemFrame refFrame : itemFrames) {
                refFrame.place(viewPoint, viewDirection);
            }
        } catch (Exception ex) {
            dismantle(viewPoint, viewDirection);
            throw new DesignBuildException("Failed to place blocks and item frames.", ex);
        }
    }

    public void build(Player player, Settings settings) throws Exception {
        build(player.getLocation(), Direction.getViewDirection(player), settings);
    }

    public void dismantle(Location viewPoint, Direction viewDirection) {
        for (ReferenceItemFrame refFrame : itemFrames) {
            ItemFrame frame = refFrame.toBukkitItemFrame(viewPoint, viewDirection);

            if (frame != null) {
                frame.remove();
            }
        }

        sign.toBukkitBlock(viewPoint, viewDirection).setType(Material.AIR);
        slot.toBukkitBlock(viewPoint, viewDirection).setType(Material.AIR);

        for (ReferenceBlock refBlock : blocks) {
            refBlock.toBukkitBlock(viewPoint, viewDirection).setType(Material.AIR);
        }
    }

    public void saveFile(File directory) throws IOException {
        FileUtils.saveJson(new File(directory, getFileName()), this);
    }

    public void deleteFile(File directory) throws IOException {
        File file = new File(directory, getFileName());
        if (!file.exists()) {
            return;
        }

        Files.delete(file.toPath());
    }

    public void reloadFile(File directory) throws IOException, JsonIOException, JsonSyntaxException {
        Design design = fromFile(new File(directory, getFileName()));
        name = design.name;
        blocks = design.blocks;
        itemFrames = design.itemFrames;
        sign = design.sign;
        slot = design.slot;
        region = design.region;
        initialDirection = design.initialDirection;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFileName() {
        return name + FILE_EXTENSION;
    }

    public ReferenceItemFrame[] getItemFrames() {
        return itemFrames;
    }

    /**
     * ItemFrameの個数を取得
     * @return 3（従来版）または9（3×3グリッド版）
     */
    public int getItemFrameCount() {
        return itemFrames != null ? itemFrames.length : 0;
    }

    /**
     * 3×3グリッド対応版かどうか判定
     * @return 9個のItemFrameを持つ場合true
     */
    public boolean is3x3Grid() {
        return itemFrames != null && itemFrames.length == ITEM_FRAME_COUNT_3x3;
    }

    public ReferenceBlock getSign() {
        return sign;
    }

    public ReferenceBlock getSlot() {
        return slot;
    }

    public ReferenceCuboid getRegion() {
        return region;
    }

    public boolean isDefault() {
        return name.equals(DEFAULT_NAME);
    }
}
