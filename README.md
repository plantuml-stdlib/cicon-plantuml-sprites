# Cloudinsight PlantUML sprites

This repository contains PlantUML sprites generated from [Cloudinsight icons](https://github.com/cloudinsight/cicon), which can easily be used in PlantUML diagrams for nice visual representation of popular technologies.

This project is inspired in other PlantUML sprites repositories like [AWS-PlantUML](https://github.com/milo-minderbinder/AWS-PlantUML) and [PlantUML Icon-Font Sprites](https://github.com/tupadr3/plantuml-icon-font-sprites).

## Usage

Just import the proper sprite into your PlantUML diagram and use it like any other sprite.

Example:

```
@startuml

!define SPRITESURL https://raw.githubusercontent.com/rabelenda/cicon-plantuml-sprites/v1.0/sprites
!includeurl SPRITESURL/tomcat.puml
!includeurl SPRITESURL/kafka.puml
!includeurl SPRITESURL/java.puml
!includeurl SPRITESURL/cassandra.puml

title Cloudinsight sprites example

skinparam monochrome true

rectangle "<$tomcat>\nwebapp" as webapp
queue "<$kafka>" as kafka
rectangle "<$java>\ndaemon" as daemon
database "<$cassandra>" as cassandra

webapp -> kafka
kafka -> daemon
daemon --> cassandra 

@enduml
```

![Example](http://www.plantuml.com/plantuml/png/VO-_JiCm4CPtFuLRiIQ15Q6g4c90Oa0jR2mN-zgOs6TZdriU7zU98XZeOj-VttrttpOnHCxE2h7IU324Sl-wUtvxsh_lNkJ07D9zrqKujqV3G-vpmgWyCHAtohlIwT4YQUYKAVKcMA2BN9D2D8ofEzsrjzsSY9KXPRF67EslDHrxfu3RKER-GqOuZ72L-8JJTK11Ia0wpfHWmnRbe_LP6qhcEB2SxvJu3IvO52ADXW94SD9vycg8tg7ac4ihecAWOv5OFznC3ZOVTCOUGbXBI78AnLV6N9bgOLDRDVohS3c_p6l0igb2KoWqCFIGAhHOc6IML8XfcMmsitPzIxger4ysfR9CII6sI3ex-mC0)

The list of available sprites is [here](sprites-list.md).

You can play around and test ideas with [PlantText](https://www.planttext.com/).

## Build

Sprites are built with provided [svgFont2plantUmlSprites.groovy](svgFont2plantUmlSprites.groovy) script. To update sprites from icons in [Cloudinsight icons](https://github.com/cloudinsight/cicon) just re-run:

```bash
./svgFont2plantUmlSprites.groovy https://raw.githubusercontent.com/cloudinsight/cicon/master/iconfont.svg
```

## Note

* All logo icons are the registered trademarks of their respective owners.
