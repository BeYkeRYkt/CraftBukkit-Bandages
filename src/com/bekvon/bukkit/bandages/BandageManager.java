/*     */ package com.bekvon.bukkit.bandages;
/*     */ 
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
/*     */ 
/*     */ public class BandageManager
/*     */ {
/*  25 */   private final Object sync = new Object();
/*     */   private Map<Player, Player> playerMap;
/*     */   private Map<Player, Player> recieverPlayerMap;
/*     */   private Map<Player, Long> timeStamp;
/*     */   private Thread runThread;
/*     */   private int itemId;
/*     */   private int amountRequired;
/*     */   private int healamount;
/*     */   private int timedelay;
/*     */   private int maxhealth;
/*     */   private boolean enabled;
/*     */   private boolean movementAllowed;
/*     */ 
/*     */   public BandageManager()
/*     */   {
/*  40 */     this.playerMap = Collections.synchronizedMap(new HashMap());
/*  41 */     this.recieverPlayerMap = Collections.synchronizedMap(new HashMap());
/*  42 */     this.timeStamp = Collections.synchronizedMap(new HashMap());
/*     */   }
/*     */ 
/*     */   public void playerMovement(Player player)
/*     */   {
/*  47 */     if (!this.movementAllowed)
/*     */     {
/*  49 */       synchronized (this.sync)
/*     */       {
/*  51 */         Player sender = null;
/*  52 */         Player reciever = null;
/*  53 */         if (this.recieverPlayerMap.containsKey(player))
/*     */         {
/*  55 */           sender = (Player)this.recieverPlayerMap.remove(player);
/*  56 */           reciever = (Player)this.playerMap.remove(sender);
/*     */         }
/*  58 */         else if (this.playerMap.containsKey(player))
/*     */         {
/*  60 */           reciever = (Player)this.playerMap.remove(player);
/*  61 */           sender = (Player)this.recieverPlayerMap.remove(reciever);
/*     */         }
/*  63 */         if ((sender != null) && (reciever != null))
/*     */         {
/*  65 */           this.timeStamp.remove(sender);
/*  66 */           if (sender.isOnline())
/*  67 */             sender.sendMessage("§cMovement has cancelled bandages.");
/*  68 */           if ((reciever.isOnline()) && (!reciever.equals(sender)))
/*  69 */             reciever.sendMessage("§cMovement has cancelled bandages.");
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadConfig(Configuration config)
/*     */   {
/*  77 */     this.itemId = config.getInt("itemId", Material.PAPER.getId());
/*  78 */     this.timedelay = (config.getInt("bandageDelay", 4) * 1000);
/*  79 */     this.maxhealth = config.getInt("maxHealth", 20);
/*  80 */     this.movementAllowed = (!config.getBoolean("requireStandStill", true));
/*  81 */     this.amountRequired = config.getInt("amountRequired", 1);
/*  82 */     this.healamount = config.getInt("healAmount", 5);
/*     */   }
/*     */ 
/*     */   public void playerBandagePlayerEvent(Player sender, Player reciever)
/*     */   {
/*  87 */     ItemStack item = sender.getItemInHand();
/*  88 */     if (item.getTypeId() == this.itemId)
/*     */     {
/*  90 */       if (!Bandages.hasAuthority(sender, "bandages.use", true))
/*     */       {
/*  92 */         sender.sendMessage("§cYou cant use bandages.");
/*  93 */         return;
/*     */       }
/*  95 */       if (reciever.getHealth() >= this.maxhealth)
/*     */       {
/*  97 */         sender.sendMessage("§cTarget is already at full heatlh!");
/*  98 */         return;
/*     */       }
/*     */ 
/* 101 */       if (item.getAmount() < this.amountRequired)
/*     */       {
/* 103 */         sender.sendMessage("§cYou are not holding enough bandages in your hand.");
/* 104 */         return;
/*     */       }
/* 106 */       synchronized (this.sync)
/*     */       {
/* 108 */         if (this.playerMap.containsKey(sender))
/*     */         {
/* 110 */           sender.sendMessage("§cYou are already bandaging somone.");
/* 111 */           return;
/*     */         }
/* 113 */         if (this.recieverPlayerMap.containsKey(reciever))
/*     */         {
/* 115 */           sender.sendMessage("§cSomone else is already bandaging that player.");
/* 116 */           return;
/*     */         }
/* 118 */         this.timeStamp.put(sender, Long.valueOf(System.currentTimeMillis()));
/* 119 */         this.playerMap.put(sender, reciever);
/* 120 */         this.recieverPlayerMap.put(reciever, sender);
/*     */       }
/* 122 */       sender.sendMessage("§eYou begin applying bandages to §f" + reciever.getName());
/* 123 */       reciever.sendMessage(sender.getName() + "§e has begun bandaging you.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void playerBandageEvent(Player player) {
/* 128 */     ItemStack item = player.getItemInHand();
/* 129 */     if (item.getTypeId() == this.itemId) {
/* 130 */       if (!Bandages.hasAuthority(player, "bandages.use", true)) {
/* 131 */         player.sendMessage("§cYou cant use bandages.");
/* 132 */         return;
/*     */       }
/* 134 */       if (player.getHealth() >= this.maxhealth) {
/* 135 */         player.sendMessage("§cYou are already at full health!");
/* 136 */         return;
/*     */       }
/* 138 */       if (item.getAmount() >= this.amountRequired)
/* 139 */         synchronized (this.sync) {
/* 140 */           if (this.playerMap.containsKey(player)) {
/* 141 */             player.sendMessage("§cYou are already applying bandages!");
/* 142 */           } else if (this.recieverPlayerMap.containsKey(player)) {
/* 143 */             player.sendMessage("§cCan't bandage while somone is applying bandages to you!");
/*     */           } else {
/* 145 */             player.sendMessage("§aYou begin applying bandages...");
/* 146 */             this.timeStamp.put(player, Long.valueOf(System.currentTimeMillis()));
/* 147 */             this.playerMap.put(player, player);
/* 148 */             this.recieverPlayerMap.put(player, player);
/*     */           }
/*     */         }
/*     */       else
/* 152 */         player.sendMessage("§cYou are not holding enough bandages in your hand.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void startThread()
/*     */   {
/* 158 */     this.enabled = true;
/* 159 */     this.runThread = new Thread(new Runnable() {
/*     */       public void run() {
/* 161 */         while (BandageManager.this.enabled)
/*     */           try {
/* 163 */             Set set = BandageManager.this.timeStamp.entrySet();
/* 164 */             synchronized (BandageManager.this.sync) {
/* 165 */               Iterator it = set.iterator();
/* 166 */               while (it.hasNext()) {
/* 167 */                 Map.Entry next = (Map.Entry)it.next();
/* 168 */                 if (System.currentTimeMillis() - BandageManager.this.timedelay > ((Long)next.getValue()).longValue()) {
/* 169 */                   Player reciever = (Player)BandageManager.this.playerMap.remove(next.getKey());
/* 170 */                   Player sender = (Player)BandageManager.this.recieverPlayerMap.remove(reciever);
/* 171 */                   it.remove();
/* 172 */                   if ((sender != null) && (reciever != null) && (sender.isOnline()) && (reciever.isOnline()))
/*     */                   {
/* 174 */                     ItemStack item = sender.getInventory().getItemInHand();
/* 175 */                     if ((item.getTypeId() == BandageManager.this.itemId) && (item.getAmount() >= BandageManager.this.amountRequired)) {
/* 176 */                       if (item.getAmount() == BandageManager.this.amountRequired)
/*     */                       {
/* 178 */                         sender.getInventory().remove(item);
/*     */                       }
/*     */                       else
/*     */                       {
/* 182 */                         item.setAmount(item.getAmount() - BandageManager.this.amountRequired);
/*     */                       }
/* 184 */                       if (reciever.getHealth() + BandageManager.this.healamount > BandageManager.this.maxhealth)
/* 185 */                         reciever.setHealth(BandageManager.this.maxhealth);
/*     */                       else
/* 187 */                         reciever.setHealth(BandageManager.this.healamount + reciever.getHealth());
/* 188 */                       if (reciever.equals(sender)) {
/* 189 */                         sender.sendMessage("§aYou finish applying bandages.");
/*     */                       } else {
/* 191 */                         sender.sendMessage("§aYou finish applying bandages on §f" + reciever.getName());
/* 192 */                         reciever.sendMessage(sender.getName() + "§a has finished bandaging you.");
/*     */                       }
/*     */                     } else {
/* 195 */                       sender.sendMessage("§cNot enough bandages in your hands!");
/* 196 */                       if (!reciever.equals(sender))
/* 197 */                         reciever.sendMessage(sender.getName() + "§c has stopped applying bandages to you.");
/*     */                     }
/*     */                   }
/*     */                 }
/*     */               }
/*     */             }
/* 203 */             Thread.sleep(1000L);
/*     */           }
/*     */           catch (Exception ex)
/*     */           {
/*     */           }
/*     */       }
/*     */     });
/* 209 */     this.runThread.start();
/*     */   }
/*     */ 
/*     */   public void killThread()
/*     */   {
/* 214 */     this.enabled = false;
/*     */   }
/*     */ 
/*     */   public int getItemId()
/*     */   {
/* 219 */     return this.itemId;
/*     */   }
/*     */ }
