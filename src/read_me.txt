./common      - libs  -  всё то, что можно использовать отдельно - вне рамок проекта 
./core        - mosaic  -  каталог исходников, где лежит ядро всего проекта - классы мозаик
./types       - data/types  -  классы данных/эвентов/...   всё что для проекта, но не отниситься к его ядру
./platform/*  - platform specific UI main Application (classic desktop; mobile; etc..)

common, core, types - только стандартные библиотеки классов, без уклона в какую либо из UI библиотек
                    - only standard class library, without any bias in any of the UI library