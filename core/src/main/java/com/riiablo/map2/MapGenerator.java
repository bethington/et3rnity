package com.riiablo.map2;

import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.random.Random;
import com.riiablo.map2.random.Seed;
import com.riiablo.map2.util.ZoneGraph;

import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;

public class MapGenerator {
  TileGenerator tileGenerator = new TileGenerator();
  final Random random = new Random();

  public void generate(DS1 ds1) {
    Map map = new Map();
    ZoneGraph zones = map.zones;
    Zone zone = zones.claim(
        0,
        0,
        ds1.width * SUBTILE_SIZE,
        ds1.height * SUBTILE_SIZE,
        ds1.width,
        ds1.height);
    Chunk chunk = zone.get(0, 0);
    copyPrefab(chunk, ds1);
  }

  public void generate(Seed seed, int diff) {
    random.seed(seed);
  }

  void copyPrefab(Chunk chunk, DS1 ds1) {
    if (ds1.width != chunk.width) throw new IllegalArgumentException("ds1 width != chunk width");
    if (ds1.height != chunk.height) throw new IllegalArgumentException("ds1 height != chunk height");
    int[] cells = ds1.floors;
    Tile[] tiles = chunk.tiles;
    for (int i = 0, s = ds1.floorLen; i < s; i++) {
      int cell = cells[i];
      tiles[i] = tileGenerator.next(
          Orientation.FLOOR,
          DS1.Cell.mainIndex(cell),
          DS1.Cell.subIndex(cell));
    }
  }

  void copyPrefab(Chunk chunk, DS1 ds1,
      int x, int y,
      int width, int height,
      int ox, int oy,
      int sx, int sy) {

  }

  void copyPrefab(Chunk chunk, DS1 ds1, int group, int x, int y, int width, int height) {
    // create and add prefab to zone prefabs
    // copy ds1 group prefab into chunk
    // at this point should know sizing, so
    //   x,y are chunk offsets
    //   w,h are group bounds
    // can this work for ds1 portions also? need prefab x offs and y offs
    //   or make distinction between prefab groups and entire ds1 copy
  }
}