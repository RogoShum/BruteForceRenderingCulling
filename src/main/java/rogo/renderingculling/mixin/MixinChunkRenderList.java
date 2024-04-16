package rogo.renderingculling.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rogo.renderingculling.api.IDualityChunkRenderList;

import java.lang.reflect.Array;
import java.util.Arrays;

@Mixin(ChunkRenderList.class)
public class MixinChunkRenderList implements IDualityChunkRenderList {
    @Shadow(remap = false) @Final private byte[] sectionsWithGeometry;
    @Shadow(remap = false) private int sectionsWithGeometryCount;
    @Shadow(remap = false) @Final private byte[] sectionsWithEntities;
    @Shadow(remap = false) @Final private byte[] sectionsWithSprites;
    @Shadow(remap = false) private int sectionsWithEntitiesCount;
    @Shadow(remap = false) private int sectionsWithSpritesCount;
    @Shadow(remap = false) private int size;
    @Shadow(remap = false) private int lastVisibleFrame;

    private final byte[] waveSectionsWithGeometry = new byte[256];
    private int waveSectionsWithGeometryCount = 0;
    private final byte[] waveSectionsWithSprites = new byte[256];
    private int waveSectionsWithSpritesCount = 0;
    private final byte[] waveSectionsWithEntities = new byte[256];
    private int waveSectionsWithEntitiesCount = 0;
    private int waveSize;
    private int lastWaveFrame;

    @Override
    public void unobserved(RenderSection render) {
        if (this.waveSize >= 256) {
            throw new ArrayIndexOutOfBoundsException("Render list is full");
        } else {
            ++this.waveSize;
            int index = render.getSectionIndex();
            int flags = render.getFlags();
            this.waveSectionsWithGeometry[this.waveSectionsWithGeometryCount] = (byte)index;
            this.waveSectionsWithGeometryCount += flags >>> 0 & 1;
            this.waveSectionsWithSprites[this.waveSectionsWithSpritesCount] = (byte)index;
            this.waveSectionsWithSpritesCount += flags >>> 2 & 1;
            this.waveSectionsWithEntities[this.waveSectionsWithEntitiesCount] = (byte)index;
            this.waveSectionsWithEntitiesCount += flags >>> 1 & 1;
        }
    }

    @Override
    public void original(int frame) {
        this.waveSectionsWithGeometryCount = 0;
        this.waveSectionsWithSpritesCount = 0;
        this.waveSectionsWithEntitiesCount = 0;
        this.waveSize = 0;
        this.lastWaveFrame = frame;
    }

    @Override
    public int getLastWaveFrame() {
        return lastWaveFrame;
    }

    @Override
    public void observer(int frame) {
        System.arraycopy(this.waveSectionsWithGeometry, 0, this.sectionsWithGeometry, 0, 256);
        this.sectionsWithGeometryCount = this.waveSectionsWithGeometryCount;
        System.arraycopy(this.waveSectionsWithSprites, 0, this.sectionsWithSprites, 0, 256);
        this.sectionsWithSpritesCount = this.waveSectionsWithSpritesCount;
        System.arraycopy(this.waveSectionsWithEntities, 0, this.sectionsWithEntities, 0, 256);
        this.sectionsWithEntitiesCount = this.waveSectionsWithEntitiesCount;
        this.size = this.waveSize;
        this.lastVisibleFrame = frame;
    }
}
