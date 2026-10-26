# Voltliz Client

Cliente de Minecraft para **Fabric 1.21.11** con Click GUI y HUD personalizable.

## Uso

- **Right Shift** abre la Click GUI (se puede cambiar en Opciones > Controles > Voltliz Client).
- Clic izquierdo en un módulo: activar/desactivar. Clic derecho: abrir sus ajustes.
- Arrastra los encabezados para mover los paneles; la rueda del ratón los desplaza.
- El botón **Edit HUD** abre el editor: arrastra los elementos, usa la rueda para cambiar su tamaño y clic derecho para restablecerlo.
- Cada módulo tiene un ajuste **Bind** para asignarle una tecla (Escape o Retroceso la borran).

La configuración se guarda en `config/voltliz/config.json`.

## Módulos

| Categoría | Módulos |
|-----------|---------|
| HUD | Watermark, FPS, CPS, Coordinates, Direction, Ping, Server Address, Clock, Keystrokes, Armor Status, Potion Effects, Module List |
| Player | Toggle Sprint |
| Client | ClickGUI (color de acento, modo arcoíris, fondo, descripciones) |

## Compilar

Requiere JDK 21 o superior.

```sh
./gradlew build
```

El `.jar` queda en `build/libs/`. Cópialo a la carpeta `mods` junto con
[Fabric API](https://modrinth.com/mod/fabric-api) para 1.21.11.
Para probarlo desde el código: `./gradlew runClient`.

## Créditos

La interfaz usa la fuente [Poppins](https://github.com/itfoundry/Poppins), con licencia SIL Open Font License 1.1 (ver `POPPINS-OFL.txt`).
