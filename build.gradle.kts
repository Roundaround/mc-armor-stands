plugins {
  id("me.roundaround.allay")
}

allay {
  displayName.set("Armor Stands")
  description.set("Place, pose, and dress armor stands with an easy-to-use UI.")
  authors.set(listOf("Roundaround"))
  license.set("MIT")
  homepage.set("https://modrinth.com/mod/armor-stands")
  repository.set("https://github.com/Roundaround/mc-armor-stands")
  issues.set("https://github.com/Roundaround/mc-armor-stands/issues")
  logoFile.set("assets/armorstands/banner.png")

  modrinth {
    projectId.set("armor-stands")
  }

  curseforge {
    projectId.set(1295440)
  }

  release {
    versionType.set("release")
    sourcesJar.set(true)
  }
}
