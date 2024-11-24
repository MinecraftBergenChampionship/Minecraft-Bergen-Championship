## Version Update Tutorial

1. Create a backup with: <br>
   `scp [server]:~ [/local/backup/path] -r`
2. Follow the instructions [here](https://docs.papermc.io/paper/updating) to update the paper server:
    * `cd` into `plugins`, `mkdir update`, then `cp` each plugin (not `MBC.jar`) into `update`
    *  Download latest stable `paper.jar` (and rename it), then `scp paper.jar [server]:/home/MBCAdmin/MBC/`
3. Update `pom.xml`; Here are some references:
    * [Commit from 1.20.4 to 1.21.1](https://github.com/MinecraftBergenChampionship/Minecraft-Bergen-Championship/commit/ace335d8851820ad8be5d429d1be5eff521b6958)
    * [Commit from 1.18.2 to 1.20.4](https://github.com/MinecraftBergenChampionship/Minecraft-Bergen-Championship/commit/eff9dc8f9f53d173fcfa792867b640c7bd9badf5)