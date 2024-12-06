Repositorio para desarrollar la tarea 2 del ramo IA para juegos.


Para esta tarea, ud. trabajará con el ejercicio MiniDungeons propuesto por los autores del
libro guía del curso en https://gameaibook.org/exercises/ . MiniDungeons es un juego
rogue-like basado en turnos, implementado por Holmgard, Liapis, Togelius y Yannakakis
(autores del libro) y utilizado como plataforma de prueba para modelar los distintos estilos
de toma de decisiones de jugadore humanos. En cada nivel de MinDungeon, el héroe
(controlado por el jugador) comienza en la entrada del nivel y debe navegar a la salida (exit)
del nivel a medida que recoleta tesoros, elimina monstruos y se recupera con pociones
medicinales. En esta tarea, ud debe desarrollar un conjunto de jugadores IA que sean
capaces de completar todos los calabozos en el simulador de MiniDungeons. El repositorio
esta completamente escrito en Java y provee una versión básica ASCII del juego completo,
lo que permite que sea fácil de usar y rápido de simular.
Lo que ud. debe hacer es:
1.- Descargar el framework en Java de MiniDungeons: Desde el repositorio:
https://github.com/sentientdesigns/minidungeons . Puede utilizar un IDE Java como Eclipse
o NetBeans para cargar las fuentes y añadir las bibliotecas. Yo utilicé Eclipse y especifiqué
JavaSE9 al momento de crear el proyecto. Creé un nuevo proyecto e importé tanto los
archivos de la carpeta src, el .jar de la carpeta lib y la carpeta dungeons con los mapas.
2.- La forma más fácil de evaluar el código es mediante el uso de las tres clases principales
en el paquete experiment:
● SimulationMode realiza un número de simulaciones de un agente IA
específico en uno o más calabozos, y reporta los resultados como métricas y
mapas de calor (heatmaps) de cada playthrough. Estos reportes también
quedan guardados en una carpeta especificada en la variable outputFolder.
● DebugMode evalúa una simulación en un mapa, paso a paso, permitiendo a
los jugadores observar lo que el agente realiza en cada acción, mostrando el
mapa ASCII y el nivel de vida (HP) actual del jugador. Información adicional
para el debugging puede incluirse, de ser necesaria.
● CompetitionMode tiene como objetivo evaluar como cada agente
especificado en el array controllerNames se desempeña contra cada otro
agente en un conjunto de métricas como tesoros recolectados, monstruos
eliminados, etc. Este modo fue creado para ser usado en competencias
internas donde los agentes creados por distintos usuarios compiten en una o
más dimensiones monitoreadas por el sistema.
3.- Implemente dos agentes para MiniDungeon, utilizando dos métodos de Inteligencia
Artificial vistos en el curso.
● Hint: Ud puede utilizar métodos de IA con templates disponibles en Java
como Best First Search, Monte Carlo Tree Search y Q-learning. Si desea,
también puede implementar otros métodos híbridos como neuroevolución.
Ejemplos de implementaciones de Q-learning y neuroevolución para
MiniDungeons se describen en este paper y este paper respectivamente.
● Recuerde revisar los apuntes de cada método visto en clase
**Esta tarea se puede hacer de manera grupal (mismos grupos que el proyecto)**. Uds
deben enviar:
- El código de su agente (controlador) con los dos métodos solicitados.
- El reporte de desempeño de su agente (que se graba en la carpeta especificada en
la variable outputFolder) para compararlo con el de los demás grupos en las
métricas: número de monstruos eliminados, tesoros recolectados, niveles
completados
