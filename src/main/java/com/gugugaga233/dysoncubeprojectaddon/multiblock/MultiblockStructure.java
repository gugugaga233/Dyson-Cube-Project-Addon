/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.gugugaga233.dysoncubeprojectaddon.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultiblockStructure {
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final VoxelShape shape;

    public MultiblockStructure(int sizeX, int sizeY, int sizeZ, VoxelShape shape) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.shape = shape;
    }

    public int getSizeX() {
        return this.sizeX;
    }

    public int getSizeY() {
        return this.sizeY;
    }

    public int getSizeZ() {
        return this.sizeZ;
    }

    public boolean validateSpace(LevelAccessor level, BlockPos anchor) {
        int sizeX = this.getSizeX();
        int sizeY = this.getSizeY();
        int sizeZ = this.getSizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return false;
        }
        int halfX = sizeX / 2;
        int halfZ = sizeZ / 2;
        BlockPos min = anchor.offset(-halfX, 0, -halfZ);
        for (int x = 0; x < sizeX; ++x) {
            for (int y = 0; y < sizeY; ++y) {
                for (int z = 0; z < sizeZ; ++z) {
                    if (level.getBlockState(min.offset(x, y, z)).isAir()) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public AABB getAABB(BlockPos anchor) {
        return new AABB((double)(anchor.getX() - this.sizeX), (double)anchor.getY(), (double)(anchor.getZ() - this.sizeZ), (double)(anchor.getX() + this.sizeX), (double)(anchor.getY() + this.sizeY), (double)(anchor.getZ() + this.sizeZ));
    }

    public VoxelShape getShape() {
        return this.shape;
    }
}


