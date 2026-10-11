private static void ensureShields(Unit u) {
    if (shieldMap.containsKey(u)) return;
    initUnitPaths(u);
    Seq<Shield> shields = new Seq<>();
    for (int i = 0; i < 5; i++) {
        shields.add(new Shield(i, u));
    }
    CirclePath a = pathAMap.get(u);
    CirclePath b = pathBMap.get(u);

    // 立即设置护盾位置，避免从原点飞入
    for (int i = 0; i < 3; i++) {
        if (a != null) {
            Shield s = shields.get(i);
            s.currentPath = a;
            float t = i / 3f;
            s.pathParam = t;
            s.position.set(a.pointAt(t));
            s.tangent.set(a.tangentAt(t));
            s.transitionProgress = 1f;
        }
    }
    for (int i = 3; i < 5; i++) {
        if (b != null) {
            Shield s = shields.get(i);
            s.currentPath = b;
            float t = (i - 3) / 2f;
            s.pathParam = t;
            s.position.set(b.pointAt(t));
            s.tangent.set(b.tangentAt(t));
            s.transitionProgress = 1f;
        }
    }
    shieldMap.put(u, shields);
}
