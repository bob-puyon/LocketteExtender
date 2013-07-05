package bob_puyon.LocketteExtender;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.yi.acru.bukkit.Lockette.Lockette;

public class TransactionListener implements Listener{

	private LocketteExtender plg;
	private static List<Material> trustee = new ArrayList<Material>();

	TransactionListener(LocketteExtender instance) {
		this.plg = instance;
	}

	// Locketteで保護対象でありアイテムインベントリーを持つもの
	static{
		trustee.add(Material.CHEST);
		trustee.add(Material.TRAPPED_CHEST);
		trustee.add(Material.BREWING_STAND); //醸造台
		trustee.add(Material.DISPENSER);
		trustee.add(Material.DROPPER);
		trustee.add(Material.HOPPER);
		trustee.add(Material.FURNACE);
		trustee.add(Material.BURNING_FURNACE); //燃焼中かまど
		trustee.add(Material.BEACON);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryMoveItemEvent(InventoryMoveItemEvent event){
		Block target;
		Entity thief;

		// アイテムの移動先と移動元を取得
		Inventory dst_inv = event.getDestination();
		Inventory src_inv = event.getSource();

		// Tips : instanceof演算子の乱用はパフォーマンス低下の原因
		// ******************
		//    窃盗判定開始
		// ******************

		// アイテムの移動先がホッパーカートで無い場合は終了
		if( dst_inv.getHolder() instanceof HopperMinecart ){
			thief = (Entity)dst_inv.getHolder();
		}else{
			return;
		}

		if( src_inv.getSize() == 54 ){
			// instanceofを避けるため、DoubleChestに関してはサイズ判断
			DoubleChest dc = (DoubleChest)src_inv.getHolder();
			target = dc.getLocation().getBlock();
		}else if( src_inv.getHolder() instanceof BlockState ){
			target = ((BlockState)src_inv.getHolder()).getBlock();
			// 移動元のブロックがインベントリを持つブロックでなければ終了
			if( !trustee.contains(target.getType()) ){ return; }
		}else{
			return;
		}

		// 対象のインベントリが共有系だった場合は無視
		if( Lockette.isEveryone(target) ){ return; }

		if( Lockette.isProtected(target) ){
			event.setCancelled( true );
			thief.remove();

			// 原因となるレールを除去(真下で半永久的に吸い出されてしまうため中止)
			/*
			Block factor = target.getRelative(BlockFace.DOWN);
			if( factor.getType().equals(Material.RAILS) ){
				factor.setType(Material.AIR);
			}
			*/

			notifyStorageOwner( target );
			notifyOperators( target );
		}
	}

	// 保護ブロックに対してホッパーチェストの窃盗が試みられた事を利用者に通知
	// （注）吸い出し周期が短い場合、プレイヤーへの意図的スパムに利用できる
	private void notifyStorageOwner(Block bk){
		for( Player p : Bukkit.getOnlinePlayers() ){
			if( Lockette.isOwner( bk, p.getName()) ){
				p.sendMessage( "§e=== [窃盗情報検知] ===" );
				p.sendMessage( "   あなたが保護しているアイテムが吸い出されようとしました" );
				p.sendMessage( "   ワールド[§6 " +bk.getWorld().getName()+ " §F]の座標を参考に現場を確認してください" );
				p.sendMessage( "   保護ブロックの位置 = [ §6x:"+bk.getX() +" y:"+bk.getY() +" z:"+bk.getZ() +" §F]" );
			}
		}
	}

	// 保護ブロックに対してホッパーチェストの窃盗が試みられた事を利用者に通知
	private void notifyOperators(Block bk){
		for( Player p : Bukkit.getOnlinePlayers() ){
			p.sendMessage( "§e [LocketteExtender] §FHopperCart Threat Detected" );
			p.sendMessage( "    World : §6" + bk.getWorld().getName());
			p.sendMessage( "    Coordinate : §6x:"+bk.getX() +" y:"+bk.getY() +" z:"+bk.getZ() );
		}
	}
}


