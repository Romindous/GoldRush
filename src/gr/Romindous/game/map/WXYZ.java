package gr.Romindous.game.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import ru.komiss77.modules.world.XYZ;
 
public class WXYZ extends XYZ {

	public final World w;
	   
	public WXYZ(final Block b) {
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
		this.w = b.getWorld();
		this.pitch = 0;
		this.yaw = 0;
	}
   
	public WXYZ(final Block b, final int i) {
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
		this.w = b.getWorld();
		this.pitch = i;
		this.yaw = 0;
	}
	   
	public WXYZ(final Block b, final int i, final int j) {
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
		this.w = b.getWorld();
		this.pitch = i;
		this.yaw = j;
	}
	   
	public WXYZ(final Location loc, final boolean dir) {
		this.x = loc.getBlockX();
		this.y = loc.getBlockY(); 
		this.z = loc.getBlockZ();
		this.w = loc.getWorld();
		this.pitch = dir ? (int) loc.getPitch() : 0;
		this.yaw = dir ? (int) loc.getYaw() : 0;
	}
	   
	public WXYZ(final XYZ p) {
		this.x = p.x;
		this.y = p.y; 
		this.z = p.z;
		this.w = Bukkit.getWorld(p.worldName);
		this.pitch = p.pitch;
		this.yaw = p.yaw;
	}
	   
	public WXYZ(final World w, final XYZ p) {
		this.x = p.x;
		this.y = p.y; 
		this.z = p.z;
		this.w = w;
		this.pitch = p.pitch;
		this.yaw = p.yaw;
	}
	   
	public WXYZ(final World w, final int x, final int y, final int z) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.pitch = 0;
		this.yaw = 0;
	}
	
	public WXYZ(final World w, final int x, final int y, final int z, final int i) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.pitch = i;
		this.yaw = 0;
	}
	
	public WXYZ(final World w, final int x, final int y, final int z, final int i, final int j) {
		this.x = x;
		this.y = y; 
		this.z = z;
		this.w = w;
		this.pitch = i;
		this.yaw = j;
	}
	
	public WXYZ add(final int x, final int y, final int z) {
		return new WXYZ(w, this.x + x, this.y + y, this.z + z, this.pitch, this.yaw);
	}
   
	public Block getBlock() {
		return this.w.getBlockAt(x, y, z);
	}
	
	@Override
	public Location getCenterLoc() {
		return new Location(w, x + 0.5d, y + 0.5d, z + 0.5d);
	}
	
	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public int dist2DSq(final WXYZ at) {
		final int dx = at.x - x, dz = at.z - z;
		return dx*dx + dz*dz;
	}
}