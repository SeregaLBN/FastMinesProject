./src_common        - libs  -  всё то, что можно использовать отдельно - вне рамок проекта 
./src_core          - mosaic  -  каталог исходников, где лежит ядро всего проекта - классы мозаик
./src_data          - data/types  -  классы данных/эвентов/...   всё что для проекта, но не отниситься к его ядру
./src_platform_xxx  - platform specific UI main Application (classic desktop; mobile; etc..)

src_common, src_core, src_data - только стандартные библиотеки классов, без уклона в какую либо из UI библиотек
                               - only standard class library, without any bias in any of the UI library

Agreements:
 * basic package/namespace name - fmg.*     -  FastMinesGame
 * using 3 space   for  tab size
 * subdirectories corresponds to the package name/namespace (С++, C# attention  -  Java style)
 * namespaces with lowercase (C# sorry)    