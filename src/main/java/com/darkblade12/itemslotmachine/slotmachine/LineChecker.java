package com.darkblade12.itemslotmachine.slotmachine;

import org.bukkit.Material;

/**
 * リアルなスロットマシンの複数ライン判定を実装
 * 中央の3リール横一行 + 右上がり斜め + 右下がり斜めをサポート
 */
public class LineChecker {
    
    /**
     * パターンが横一行で全て同じかチェック
     * @param pattern 3つのリール結果 [左, 中, 右]
     * @return 全て同じ場合true
     */
    public static boolean isHorizontalWin(Material[] pattern) {
        if (pattern.length != 3) {
            return false;
        }
        return pattern[0] == pattern[1] && pattern[1] == pattern[2];
    }
    
    /**
     * 複数ラインのいずれかでウィンしているかチェック
     * @param pattern 3つのリール結果 [左, 中, 右]
     * @return いずれかのラインがウィンしている場合true
     */
    public static boolean isMultiLineWin(Material[] pattern) {
        if (pattern.length != 3) {
            return false;
        }
        
        // 横ラインチェック（従来の判定）
        if (isHorizontalWin(pattern)) {
            return true;
        }
        
        // 注: 3リールのみなので、斜めラインは実装不可
        // 3×3グリッド（9リール）に拡張する場合はここで斜めラインチェックを追加
        
        return false;
    }
    
    /**
     * 3×3グリッド版：複数ラインの判定
     * 使用予定：将来的に3×3グリッドに対応する場合
     * @param grid 3×3のリール結果 [[上左, 上中, 上右], [中左, 中中, 中右], [下左, 下中, 下右]]
     * @return いずれかのラインがウィンしている場合true
     */
    public static boolean isGridMultiLineWin(Material[][] grid) {
        if (grid == null || grid.length != 3 || grid[0].length != 3) {
            return false;
        }
        
        // 中央の横ラインチェック（現在の1ラインのみ対応）
        if (isCentralHorizontalWin(grid)) {
            return true;
        }
        
        // 右上がり斜めラインチェック（左下 → 中央 → 右上）
        if (isAscendingDiagonalWin(grid)) {
            return true;
        }
        
        // 右下がり斜めラインチェック（左上 → 中央 → 右下）
        if (isDescendingDiagonalWin(grid)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 中央の横ライン（真ん中の列が全て同じ）をチェック
     * @param grid 3×3グリッド
     * @return 中央横ラインがウィンしている場合true
     */
    public static boolean isCentralHorizontalWin(Material[][] grid) {
        Material center = grid[1][0]; // 中央列の左
        return center != null && center != Material.AIR
            && center.equals(grid[1][1])  // 中央列の中
            && center.equals(grid[1][2]); // 中央列の右
    }
    
    /**
     * 右上がり斜めライン（左下 → 中央 → 右上）をチェック
     * @param grid 3×3グリッド
     * @return 右上がり斜めラインがウィンしている場合true
     */
    public static boolean isAscendingDiagonalWin(Material[][] grid) {
        Material start = grid[2][0]; // 左下
        return start != null && start != Material.AIR
            && start.equals(grid[1][1])  // 中央
            && start.equals(grid[0][2]); // 右上
    }
    
    /**
     * 右下がり斜めライン（左上 → 中央 → 右下）をチェック
     * @param grid 3×3グリッド
     * @return 右下がり斜めラインがウィンしている場合true
     */
    public static boolean isDescendingDiagonalWin(Material[][] grid) {
        Material start = grid[0][0]; // 左上
        return start != null && start != Material.AIR
            && start.equals(grid[1][1])  // 中央
            && start.equals(grid[2][2]); // 右下
    }
}
