./src_common        - libs  -  всё то, что можно использовать отдельно - вне рамок проекта 
./src_core          - mosaic  -  каталог исходников, где лежит ядро всего проекта - классы мозаик
./src_types         - data/types  -  классы данных/эвентов/...   всё что для проекта, но не отниситься к его ядру
./src_platform_xxx  - platform specific UI main Application (classic desktop; mobile; etc..)

common, core, types - только стандартные библиотеки классов, без уклона в какую либо из UI библиотек
                    - only standard class library, without any bias in any of the UI library