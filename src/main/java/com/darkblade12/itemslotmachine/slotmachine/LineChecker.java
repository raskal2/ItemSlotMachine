package com.darkblade12.itemslotmachine.slotmachine;

import org.bukkit.Material;

/**
 * リアルなスロットマシンの複数ライン判定（3×3グリッド対応）
 * 
 * グリッド構造:
 *   [0][0] [0][1] [0][2]   (上段)
 *   [1][0] [1][1] [1][2]   (中央行)
 *   [2][0] [2][1] [2][2]   (下段)
 * 
 * サポートするライン:
 * 1. 上段の横ライン: [0][0] == [0][1] == [0][2]
 * 2. 中央の横ライン: [1][0] == [1][1] == [1][2]
 * 3. 下段の横ライン: [2][0] == [2][1] == [2][2]
 * 4. 右上がり斜めライン: [2][0] == [1][1] == [0][2]
 * 5. 右下がり斜めライン: [0][0] == [1][1] == [2][2]
 */
public class LineChecker {
    
    /**
     * 3×3グリッド全体での複数ライン判定
     * @param grid 3×3のリール結果
     * @return いずれかのラインがウィンしている場合true
     */
    public static boolean isMultiLineWin(Material[][] grid) {
        if (grid == null || grid.length != 3 || grid[0].length != 3) {
            return false;
        }
        
        // 3つの横ラインをチェック
        if (isTopHorizontalWin(grid) || isCentralHorizontalWin(grid) || isBottomHorizontalWin(grid)) {
            return true;
        }
        
        // 2つの斜めラインをチェック
        if (isAscendingDiagonalWin(grid) || isDescendingDiagonalWin(grid)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 上段の横ライン（[0][0], [0][1], [0][2]）をチェック
     * @param grid 3×3グリッド
     * @return 上段がウィンしている場合true
     */
    public static boolean isTopHorizontalWin(Material[][] grid) {
        Material top = grid[0][0];
        return top != null && top != Material.AIR
            && top.equals(grid[0][1])
            && top.equals(grid[0][2]);
    }
    
    /**
     * 中央の横ライン（[1][0], [1][1], [1][2]）をチェック
     * @param grid 3×3グリッド
     * @return 中央がウィンしている場合true
     */
    public static boolean isCentralHorizontalWin(Material[][] grid) {
        Material center = grid[1][0];
        return center != null && center != Material.AIR
            && center.equals(grid[1][1])
            && center.equals(grid[1][2]);
    }
    
    /**
     * 下段の横ライン（[2][0], [2][1], [2][2]）をチェック
     * @param grid 3×3グリッド
     * @return 下段がウィンしている場合true
     */
    public static boolean isBottomHorizontalWin(Material[][] grid) {
        Material bottom = grid[2][0];
        return bottom != null && bottom != Material.AIR
            && bottom.equals(grid[2][1])
            && bottom.equals(grid[2][2]);
    }
    
    /**
     * 右上がり斜めライン（[2][0], [1][1], [0][2]）をチェック
     * 左下 → 中央 → 右上
     * @param grid 3×3グリッド
     * @return 右上がり斜めがウィンしている場合true
     */
    public static boolean isAscendingDiagonalWin(Material[][] grid) {
        Material start = grid[2][0]; // 左下
        return start != null && start != Material.AIR
            && start.equals(grid[1][1])  // 中央
            && start.equals(grid[0][2]); // 右上
    }
    
    /**
     * 右下がり斜めライン（[0][0], [1][1], [2][2]）をチェック
     * 左上 → 中央 → 右下
     * @param grid 3×3グリッド
     * @return 右下がり斜めがウィンしている場合true
     */
    public static boolean isDescendingDiagonalWin(Material[][] grid) {
        Material start = grid[0][0]; // 左上
        return start != null && start != Material.AIR
            && start.equals(grid[1][1])  // 中央
            && start.equals(grid[2][2]); // 右下
    }
    
    /**
     * ウィンしたラインの種類を取得（デバッグ用）
     * @param grid 3×3グリッド
     * @return ウィンしたラインの説明文字列（複数行、最後にnullを含む配列）
     */
    public static String[] getWinLines(Material[][] grid) {
        if (grid == null || grid.length != 3 || grid[0].length != 3) {
            return new String[0];
        }
        
        java.util.List<String> winLines = new java.util.ArrayList<>();
        
        if (isTopHorizontalWin(grid)) {
            winLines.add("上段の横ライン");
        }
        if (isCentralHorizontalWin(grid)) {
            winLines.add("中央の横ライン");
        }
        if (isBottomHorizontalWin(grid)) {
            winLines.add("下段の横ライン");
        }
        if (isAscendingDiagonalWin(grid)) {
            winLines.add("右上がり斜めライン");
        }
        if (isDescendingDiagonalWin(grid)) {
            winLines.add("右下がり斜めライン");
        }
        
        return winLines.toArray(new String[0]);
    }
}
