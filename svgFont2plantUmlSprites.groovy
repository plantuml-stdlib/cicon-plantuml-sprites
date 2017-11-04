#!/usr/bin/env groovy

@Grab('net.sourceforge.plantuml:plantuml:1.2017.16')
@Grab('org.apache.xmlgraphics:batik-transcoder:1.9.1')
@Grab('org.apache.xmlgraphics:batik-codec:1.9.1')

import groovy.util.XmlSlurper
import groovy.xml.MarkupBuilder
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import net.sourceforge.plantuml.ugraphic.sprite.SpriteGrayLevel
import net.sourceforge.plantuml.ugraphic.sprite.SpriteUtils
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput

final DEFAULT_SIZE = 48
final TMP_DIR = new File('/tmp/svgFont2plantUmlSprites')
TMP_DIR.mkdirs()
final SPRITES_DIR = new File('sprites')
SPRITES_DIR.mkdirs()
final PNGS_DIR = new File('pngs')
PNGS_DIR.mkdirs()
final SPRITES_LIST_FILE = new File('sprites-list.md')

def cli = new CliBuilder(usage: 'svgFont2plantUmlSprites.groovy [options] <svg URL>', stopAtNonOption: false, footer: 'Usage example: ./svgFont2plantUmlSprites.groovy https://raw.githubusercontent.com/cloudinsight/cicon/master/iconfont.svg')
cli.s(longOpt: 'size', args: 1, argName: 'size', "Size (width and height) in pixels of generated sprites. Default value: $DEFAULT_SIZE")
def options = cli.parse(args)
!options && System.exit(1)
if (!options.arguments()) {
  println "error: Missing required svg URL"
  cli.usage()
  System.exit(1)
}
if (options.arguments().size() > 1) {
  println "error: Only one svg URL is supported"
  cli.usage()
  System.exit(1)
}
def svgUrl = options.arguments()[0]
def size = options.s ?: DEFAULT_SIZE

def fontSvg = url2File(svgUrl, TMP_DIR)
def charsSvgs = fontSvg2CharsSvgs(fontSvg, size, TMP_DIR)
def charsPngs = charsSvgs.collect {
  charSvg2Png(it, PNGS_DIR)
}
charsPngs.each {
  png2PlantUmlSprite(it, SPRITES_DIR)
}
buildSpritesListing(charsPngs, PNGS_DIR, SPRITES_LIST_FILE)

def url2File(url, workDir) {
  def svgFile = new File("$workDir/font.svg")
  svgFile.delete()
  svgFile << new URL(url).text
  return svgFile
}

def fontSvg2CharsSvgs(fontSvg, charSize, workDir) {
  def parser = new XmlSlurper()
  parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false) 
  parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
  
  def font = parser.parseText(fontSvg.text).defs.font
  def defaultWidth = font.@'horiz-adv-x'.text() as Integer
  def maxY = font.'font-face'.@ascent.text() as Integer
  def minY = font.'font-face'.@descent.text() as Integer
  def height = maxY - minY
  def mirrorYTranslate = maxY + minY

  font.glyph
  .findAll { it.@'glyph-name'.text() && it.@d.text() }
  .collect { glyph ->
    def width = glyph.@'horiz-adv-x'.text() ?: defaultWidth
    def svgFile = new File("$workDir/${glyph.@'glyph-name'.text()}.svg")
    svgFile.delete()
    StringWriter writer = new StringWriter()
    MarkupBuilder xml = new MarkupBuilder(writer)
    xml.svg(width: charSize, height: charSize, viewBox: "0 $minY $width $height", xmlns: "http://www.w3.org/2000/svg") {
      path(d: glyph.@d.text(), transform: "translate(0, $mirrorYTranslate) scale(1, -1)")
    }
    svgFile << writer.toString()
  }
}

def charSvg2Png(charSvg, workDir) {
  def fileName = charSvg.name.replace(".svg",".png")
  pngFile = new File("$workDir/$fileName")
  pngFile.delete()
  OutputStream pngOut = new FileOutputStream(pngFile)
  new PNGTranscoder().transcode(new TranscoderInput(charSvg.toURI().toString()), new TranscoderOutput(pngOut))
  pngOut.flush()
  pngOut.close()
  return pngFile
}

def png2PlantUmlSprite(charPng, outputDir) {
  BufferedImage im = ImageIO.read(charPng)
  removeAlpha(im)
  String spriteName = charPng.name.replace(".png","")
  def spriteFile = new File("$outputDir/${spriteName}.puml")
  spriteFile.delete()
  spriteFile << "@startuml\n" + SpriteUtils.encode(im, spriteName, SpriteGrayLevel.GRAY_16) + "@enduml\n"
}

def removeAlpha(im) {
  Graphics2D graphics = im.createGraphics()
  try {
      graphics.setComposite(AlphaComposite.DstOver)
      graphics.setPaint(Color.WHITE)
      graphics.fillRect(0, 0, im.getWidth(), im.getHeight())
  }
  finally {
      graphics.dispose()
  }
}

def buildSpritesListing(pngs, pngsPath, listFile) {
  listFile.delete()
  listFile << '''# Sprites list

| Sprite | Icon |
|--------|------|
'''
  pngs.each {
    def spriteName = it.name.replace('.png','.puml')
    listFile << "|$spriteName|![$spriteName]($pngsPath/$it.name)|\n"
  }
}
