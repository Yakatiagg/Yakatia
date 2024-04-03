Yakatia
A highly optimized Paper fork designed for large servers requiring both maximum performance, stability, and "enterprise" features.

Homepage - Downloads

Features
Sentry Integration Easily track all errors coming from your server in excruciating detail
Better Entity Performance Reduces the performance impact of entities by skipping useless work and making barely-noticeable changes to behavior
Partial Asynchronous Processing Partially offloads some heavy work to other threads where possible without sacrificing stability
8x Faster Map Rendering Reduces or eliminates lag spikes caused by plugins like ImageOnMap or ImageMaps
30% faster hoppers over Paper (Airplane)
Reduced GC times & frequency from removing useless allocations, which also improves CPU performance (Airplane)
Fast raytracing which improves performance of any entity which utilizes line of sight, mainly Villagers (Airplane)
Built-in profiler which has 0 performance hit and easy to read metrics for both server owners and developers (Airplane)
Faster crafting, reduction in uselessly loaded chunks, faster entity ticking, faster block ticking, faster bat spawning, and more!
Complete compatibility with any plugin compatible with Paper
And more coming soon...
Downloads
You can download the latest JAR file here.


Building
./gradlew build
Or building a Paperclip JAR for distribution:

./gradlew paperclip
License
Patches are licensed under GPL-3.0. All other files are licensed under MIT.
