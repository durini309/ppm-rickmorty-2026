package com.gt.uvg.rickandmorty.location.data

class LocationDb {
    private val locations: List<Location> = listOf(
        Location(1, "Earth (C-137)", "Planet", "Dimension C-137"),
        Location(2, "Abadango", "Cluster", "unknown"),
        Location(3, "Citadel of Ricks", "Space station", "unknown"),
        Location(4, "Worldender's lair", "Planet", "unknown"),
        Location(5, "Anatomy Park", "Microverse", "Dimension C-137"),
        Location(6, "Interdimensional Cable", "TV", "unknown"),
        Location(7, "Immortality Field Resort", "Resort", "unknown"),
        Location(8, "Post-Apocalyptic Earth", "Planet", "Post-Apocalyptic Dimension"),
        Location(9, "Purge Planet", "Planet", "Replacement Dimension"),
        Location(10, "Venzenulon 7", "Planet", "unknown"),
        Location(11, "Bepis 9", "Planet", "unknown"),
        Location(12, "Cronenberg Earth", "Planet", "Cronenberg Dimension"),
        Location(13, "Nuptia 4", "Planet", "unknown"),
        Location(14, "Giant's Town", "Fantasy town", "Fantasy Dimension"),
        Location(15, "Bird World", "Planet", "unknown"),
        Location(16, "St. Gloopy Noops Hospital", "Space station", "unknown"),
        Location(17, "Earth (5-126)", "Planet", "Dimension 5-126"),
        Location(18, "Mr. Goldenfold's dream", "Dream", "Dimension C-137"),
        Location(19, "Gromflom Prime", "Planet", "Replacement Dimension"),
        Location(20, "Earth (Replacement Dimension)", "Planet", "Replacement Dimension")
    )

    fun getAllLocations(): List<Location> {
        return locations
    }

    fun getLocationById(id: Int): Location {
        return locations.first { it.id == id }
    }
}