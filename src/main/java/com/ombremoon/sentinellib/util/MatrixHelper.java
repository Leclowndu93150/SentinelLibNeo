package com.ombremoon.sentinellib.util;

import com.mojang.math.Axis;
import com.ombremoon.sentinellib.api.box.BoxInstance;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Utility class used primarily for OBB translations and rotations
 */
public class MatrixHelper {
    public static final StreamCodec<ByteBuf, Matrix4f> MATRIX4F = new StreamCodec<>() {
        @Override
        public Matrix4f decode(ByteBuf byteBuf) {
            return readMatrix4f(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, Matrix4f matrix4f) {
            writeMatrix4f(byteBuf, matrix4f);
        }
    };

    /**
     * Linear transformation of a 3D vector with a 4x4 matrix
     * @param matrix4f The transformation matrix
     * @param from The vector undergoing a transformation
     * @return The transformed matrix
     */
    public static Vec3 transform(Matrix4f matrix4f, Vec3 from) {
        double d0 = matrix4f.m00() * from.x + matrix4f.m10() * from.y + matrix4f.m20() * from.z + matrix4f.m30();
        double d1 = matrix4f.m01() * from.x + matrix4f.m11() * from.y + matrix4f.m21() * from.z + matrix4f.m31();
        double d2 = matrix4f.m02() * from.x + matrix4f.m12() * from.y + matrix4f.m22() * from.z + matrix4f.m32();
        return new Vec3(d0, d1, d2);
    }

    /**
     * Returns a 4x4 matrix representing the position in space of an entity
     * @param owner The entity providing the position
     * @param partialTicks The time between ticks
     * @return A 4x4 matrix translated and rotated to an entity's position
     */
    public static Matrix4f getEntityMatrix(Entity owner, BoxInstance instance, float partialTicks) {
        Matrix4f matrix4f = new Matrix4f();
        Vec3 pos = owner.position();
        Matrix4f centerMatrix = new Matrix4f().translate((float) pos.x, (float) (pos.y + instance.getSentinelBox().getBoxOffset().y), (float) pos.z);
        matrix4f.mulLocal(centerMatrix.mul(getBoxRotation(instance, partialTicks)));
        return matrix4f;
    }

    public static Matrix4f getDynamicEntityMatrix(Entity owner, BoxInstance instance, float partialTicks) {
        SentinelBox box = instance.getSentinelBox();
        Vec3 pos = owner.position();
        Matrix4f matrix4f = new Matrix4f();
        Matrix4f centerMatrix = new Matrix4f().translate((float) pos.x, (float) pos.y, (float) pos.z);
        matrix4f.mulLocal(centerMatrix.mul(getBoxRotation(instance, partialTicks)));
        Vec3 path = box.getBoxPath(instance, partialTicks);
        matrix4f.translate((float) path.x, (float) path.y, (float) path.z);
        return matrix4f;
    }

    /**
     * Returns a 4x4 matrix representing the yaw of an entity
     * @param partialTicks The time between ticks
     * @return An identity 4x4 matrix that has been rotated on the y-axis
     */
    private static Matrix4f getBoxRotation(BoxInstance instance, float partialTicks) {
        float yRot = -instance.yRot;
        float yRot0 = -instance.yRot0;
        float xRot = instance.xRot;
        float xRot0 = instance.xRot0;
        float zRot = instance.zRot;
        float zRot0 = instance.zRot0;

        Matrix4f matrix4f = new Matrix4f();
        float yawAmount = Mth.clampedLerp(yRot0, yRot, partialTicks);
        float pitchAmount = Mth.clampedLerp(xRot0, xRot, partialTicks);
        float rollAmount = Mth.clampedLerp(zRot0, zRot, partialTicks);
        return matrix4f.rotate(Axis.YP.rotationDegrees(yawAmount)).rotate(Axis.XP.rotationDegrees(pitchAmount)).rotate(Axis.ZP.rotationDegrees(rollAmount));
    }

    public static Matrix4f getMovementMatrix(Entity owner, BoxInstance instance, float partialTicks, SentinelBox.MoverType type) {
        switch (type) {
            case CUSTOM, CUSTOM_BODY, CUSTOM_HEAD -> {
                return getDynamicEntityMatrix(owner, instance, partialTicks);
            }
            case BONE -> {
                return getBoneMatrix(instance, partialTicks);
            }
            default -> {
                return getEntityMatrix(owner, instance, partialTicks);
            }
        }
    }

    private static Matrix4f getBoneMatrix(BoxInstance instance, float partialTicks) {
        Matrix4f matrix4f = new Matrix4f();
        Vec3 pos = instance.getAnchorPoint();
        if (pos != null) {
            Matrix4f centerMatrix = new Matrix4f().translate((float) pos.x, (float) pos.y, (float) pos.z);
            matrix4f.mulLocal(centerMatrix.mul(getBoxRotation(instance, partialTicks)));
        }
        return matrix4f;
    }

    public static void translateMatrixToBone(Matrix4f matrix, GeoBone bone) {
        matrix.translate(-bone.getPosX() / 16f, bone.getPosY() / 16f, bone.getPosZ() /16f);
    }

    public static void rotateMatrixAroundBone(Matrix4f matrix, GeoBone bone) {
        if (bone.getRotZ() != 0)
            matrix.rotate(Axis.ZP.rotation(bone.getRotZ()));

        if (bone.getRotY() != 0)
            matrix.rotate(Axis.YP.rotation(bone.getRotY() + 180));

        if (bone.getRotX() != 0)
            matrix.rotate(Axis.XP.rotation(bone.getRotX()));
    }

    public static void scaleMatrixForBone(Matrix4f matrix, GeoBone bone) {
        matrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(Matrix4f matrix, GeoBone bone) {
        matrix.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
    }

    /**
     * Returns a quaternion from a 4x4 matrix. Used for client rendering.
     * @param matrix The rotated 4x4 matrix
     * @return A quaternion used by the renderer
     */
    public static Quaternionf quaternion(Matrix4f matrix) {
        float trace = matrix.m00() + matrix.m11() + matrix.m22();
        float f;
        float w;
        float x;
        float y;
        float z;
        if (trace > 0) {
            f = (float) (Math.sqrt(trace + 1.0F) * 2.0F);
            w = 0.25F * f;
            x = (matrix.m21() - matrix.m12()) / f;
            y = (matrix.m02() - matrix.m20()) / f;
            z = (matrix.m10() - matrix.m01()) / f;
        } else if (matrix.m00() > matrix.m11() && matrix.m00() > matrix.m22()) {
            f = (float) (Math.sqrt(1.0F + matrix.m00() - matrix.m11() - matrix.m22()) * 2.0F);
            w = (matrix.m21() - matrix.m12()) / f;
            x = 0.25F * f;
            y = (matrix.m01() + matrix.m10()) / f;
            z = (matrix.m02() + matrix.m20()) / f;
        } else if (matrix.m11() > matrix.m22()) {
            f = (float) (Math.sqrt(1.0F + matrix.m11() - matrix.m00() - matrix.m22()) * 2.0F);
            w = (matrix.m02() - matrix.m20()) / f;
            x = (matrix.m01() + matrix.m10()) / f;
            y = 0.25F * f;
            z = (matrix.m12() + matrix.m21()) / f;
        } else {
            f = (float) (Math.sqrt(1.0F + matrix.m22() - matrix.m00() - matrix.m11()) * 2.0F);
            w = (matrix.m10() - matrix.m01()) / f;
            x = (matrix.m02() + matrix.m20()) / f;
            y = (matrix.m12() + matrix.m21()) / f;
            z = 0.25F * f;
        }
        Quaternionf quaternionf = new Quaternionf(x, y, z, w);
        return quaternionf.normalize();
    }

    public static Vec3 project(Vec3 startVec, Vec3 endVec) {
        double projection = (endVec.dot(startVec)) / (Mth.square(endVec.x) + Mth.square(endVec.y) + Mth.square(endVec.z));
        return endVec.scale(projection);
    }

    private static Matrix4f readMatrix4f(ByteBuf buffer) {
        return new Matrix4f(
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat()
        );
    }

    private static void writeMatrix4f(ByteBuf buffer, Matrix4f matrix4f) {
        buffer.writeFloat(matrix4f.m00());
        buffer.writeFloat(matrix4f.m01());
        buffer.writeFloat(matrix4f.m02());
        buffer.writeFloat(matrix4f.m03());
        buffer.writeFloat(matrix4f.m10());
        buffer.writeFloat(matrix4f.m11());
        buffer.writeFloat(matrix4f.m12());
        buffer.writeFloat(matrix4f.m13());
        buffer.writeFloat(matrix4f.m20());
        buffer.writeFloat(matrix4f.m21());
        buffer.writeFloat(matrix4f.m22());
        buffer.writeFloat(matrix4f.m23());
        buffer.writeFloat(matrix4f.m30());
        buffer.writeFloat(matrix4f.m31());
        buffer.writeFloat(matrix4f.m32());
        buffer.writeFloat(matrix4f.m33());
    }


}
