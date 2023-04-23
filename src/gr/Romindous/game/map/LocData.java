package gr.Romindous.game.map;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class LocData {

	public final WXYZ loc;
	public final Material mt;
	public final BlockData bd;
	
	public LocData(final WXYZ loc, final BlockData bd) {
		this.loc = loc;
		this.mt = bd.getMaterial();
		this.bd = bd;
	}
	
	public LocData(final WXYZ loc, final Material mt) {
		this.loc = loc;
		this.mt = mt;
		this.bd = null;
	}
	
}
