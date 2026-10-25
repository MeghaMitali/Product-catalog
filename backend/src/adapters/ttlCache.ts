interface CacheEntry<T> {
  expiresAt: number;
  value: T;
}

export class TtlCache {
  private readonly entries = new Map<string, CacheEntry<unknown>>();

  async getOrSet<T>(
    key: string,
    ttlSeconds: number,
    load: () => Promise<T>,
  ): Promise<T> {
    const now = Date.now();
    const cached = this.entries.get(key) as CacheEntry<T> | undefined;
    if (cached && cached.expiresAt > now) return cached.value;
    if (cached) this.entries.delete(key);

    const value = await load();
    if (ttlSeconds > 0) {
      this.entries.set(key, { value, expiresAt: now + ttlSeconds * 1000 });
    }
    return value;
  }

  clear(): void {
    this.entries.clear();
  }
}
