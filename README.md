# Asumption
As the number requested is lower or equal than 10 I assumed is better to get 10, store and always ensure next cached calls will have the specified amount.

If that could not be the case, asuming the number is much higher, the implementation should consider the case where:

- First call asks for 3
- Second call asks for 10 (3 are cached and new request should be done to retrieve 7)

For that case, a composition in between the cached result and the new request should be done.

# Approach

- Infra tests ignored.
- In case of database usage some inmemory tests to proove queries shoulld be done.
- Testing is based on use cases. Based on that, CacheInMemoryServicce it's tested together with TweetService.

# Improvements

- Error handling for specific events instead of generic illegal argument exception