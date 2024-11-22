package fr.tathan.sky_aesthetics.client.skies.record;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SkyObject(ResourceLocation texture, boolean blend, float size, Vec3 rotation, Optional<Vector3f> objectRotation, int height, String rotationType) {

    public static Codec<Vector3f> VEC3F = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 3).map((listx) -> new Vector3f(listx.getFirst(), listx.get(1), listx.getLast())), (vector3f) -> List.of(vector3f.x, vector3f.y, vector3f.z));


    public static final Codec<SkyObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(SkyObject::texture),
            Codec.BOOL.fieldOf("blend").forGetter(SkyObject::blend),
            Codec.FLOAT.fieldOf("size").forGetter(SkyObject::size),
            Vec3.CODEC.fieldOf("rotation").forGetter(SkyObject::rotation),
            VEC3F.optionalFieldOf("object_rotation").forGetter(SkyObject::objectRotation),
            Codec.INT.fieldOf("height").forGetter(SkyObject::height),
            Codec.STRING.fieldOf("rotation_type").forGetter(SkyObject::rotationType)
    ).apply(instance, SkyObject::new));

    public void setObjectPosition(PoseStack poseStack, float dayAngle) {
        poseStack.mulPose(Axis.YP.rotationDegrees((float) this.rotation().y));
        if(Objects.equals(this.rotationType(), "DAY")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle));
        } else if(Objects.equals(this.rotationType(), "NIGHT")) {
            poseStack.mulPose(Axis.XP.rotationDegrees(dayAngle + 180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) this.rotation().x));
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) this.rotation().z));

    }

    public void setObjectRotation(PoseStack poseStack) {
        this.objectRotation.ifPresent((rotation) -> {
            poseStack.translate(0, 100, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z));
            poseStack.translate(0, -100, 0);
        });
    }
}
