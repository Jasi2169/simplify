.class Lpeephole_strategy_test;
.super Ljava/lang/Object;

.method public static classForName()V
  .locals 1

  :try_start_0
  invoke-static {v0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
  move-result v0
  :try_end_0
  .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

  :catch_0
  return-void
.end method

.method public static stringInit()V
  .locals 2

  invoke-direct {v0, v1}, Ljava/lang/String;-><init>([B)V

  return-void
.end method

.method public static constantPredicate()I
  .locals 1

  const/4 v0, 0x0
  if-eq v0, v0, :end

  const/4 v0, 0x1

  :end
  return v0
.end method
