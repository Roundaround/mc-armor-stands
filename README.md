![Armor Stands](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/title-round-1.png)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/armor-stands?style=flat&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/armor-stands)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1295440?style=flat&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/armor-stands)
[![GitHub Repo stars](https://img.shields.io/github/stars/Roundaround/mc-armor-stands?style=flat&logo=github)](https://github.com/Roundaround/mc-armor-stands)

[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/roundaround)

Place, pose, and dress armor stands with an easy-to-use UI — six editor screens plus a mannequin mode that renders a stand as a real player.

## Installing

Grab a build from [Modrinth](https://modrinth.com/mod/armor-stands) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/armor-stands). Fabric builds need [Fabric API](https://modrinth.com/mod/fabric-api).

## Building from source

```sh
./gradlew build
```

Dev runs are per loader: `:fabric:runClient`, `:neoforge:runClient`, `:forge:runClient`, and the `runServer` equivalents. Game tests run with `./gradlew :fabric:runClientGameTests` and `:fabric:runServerGameTests`.

The build is an [Allay](https://github.com/Roundaround/allay) consumer and bundles [Trove](https://github.com/Roundaround/trove).

Shared code lives in `common/` and is added to each loader subproject via `srcDir`.

## Contributing

Issues and pull requests are welcome at [the issue tracker](https://github.com/Roundaround/mc-armor-stands/issues).

- Branch from `main`, which tracks the newest supported Minecraft version. Older lines live on their own version-named branches.
- Keep loader-agnostic code in `common/`; only genuinely loader-specific glue belongs in a loader subproject.
- Run `./gradlew build` plus the Fabric game tests before opening a PR, and add a changelog entry under `changelogs/` named for the version you're targeting.

## License

[MIT](LICENSE)
