package openblocks.client.renderer.tileentity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import openblocks.client.StencilSkyRenderer;
import openblocks.common.block.BlockSky;
import openmods.renderer.StencilRendererHandler;
import openmods.utils.ColorUtils.RGB;
import openmods.utils.render.RenderUtils;

import org.lwjgl.opengl.GL11;

public class TileEntitySkyRenderer extends TileEntitySpecialRenderer {

	private boolean disableStencil;
	private boolean initialized;

	private int displayListBase;
	private StencilRendererHandler handler;

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTickTime) {
		if (disableStencil) return;

		int meta = te.getBlockMetadata();
		if (!BlockSky.isActive(meta)) return;

		if (!initialized) {
			intialize();
			initialized = true;
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		RGB fog = RenderUtils.getFogColor();
		GL11.glColor3f(fog.getR(), fog.getG(), fog.getB());
		GL11.glCallList(displayListBase + MinecraftForgeClient.getRenderPass()); // fancy!

		GL11.glPopMatrix();
		handler.markForRender();

	}

	protected void intialize() {
		final int stencilBit = MinecraftForgeClient.reserveStencilBit();

		if (stencilBit >= 0) {
			final int mask = 1 << stencilBit;

			displayListBase = GL11.glGenLists(2);
			GL11.glNewList(displayListBase, GL11.GL_COMPILE);
			renderCube();
			GL11.glEndList();

			GL11.glNewList(displayListBase + 1, GL11.GL_COMPILE);
			cutHoleInWorld(mask);
			GL11.glEndList();

			handler = new StencilSkyRenderer(mask);
		} else disableStencil = true;
	}

	private static void renderCube() {
		final Tessellator tes = new Tessellator();
		tes.startDrawingQuads();

		tes.addVertex(0, 0, 0);
		tes.addVertex(0, 1, 0);
		tes.addVertex(1, 1, 0);
		tes.addVertex(1, 0, 0);

		tes.addVertex(0, 0, 1);
		tes.addVertex(1, 0, 1);
		tes.addVertex(1, 1, 1);
		tes.addVertex(0, 1, 1);

		tes.addVertex(0, 0, 0);
		tes.addVertex(0, 0, 1);
		tes.addVertex(0, 1, 1);
		tes.addVertex(0, 1, 0);

		tes.addVertex(1, 0, 0);
		tes.addVertex(1, 1, 0);
		tes.addVertex(1, 1, 1);
		tes.addVertex(1, 0, 1);

		tes.addVertex(0, 0, 0);
		tes.addVertex(1, 0, 0);
		tes.addVertex(1, 0, 1);
		tes.addVertex(0, 0, 1);

		tes.addVertex(0, 1, 0);
		tes.addVertex(0, 1, 1);
		tes.addVertex(1, 1, 1);
		tes.addVertex(1, 1, 0);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderUtils.disableLightmap();
		tes.draw();
		RenderUtils.enableLightmap();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	private static void cutHoleInWorld(int stencilMask) {
		GL11.glStencilMask(stencilMask);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_ALWAYS, stencilMask, stencilMask);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		renderCube();
		GL11.glColorMask(true, true, true, true);
		GL11.glStencilMask(0);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}
}
