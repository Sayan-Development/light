package org.sayandev.xml

import org.w3c.dom.Document
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class XMLReader(private val inputStream: InputStream) {
    fun read(): Document {
        val documentBuilder = DocumentBuilderFactory.newInstance().apply {
            this.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        }.newDocumentBuilder()
        val document = documentBuilder.parse(inputStream)
        document.documentElement.normalize()
        return document
    }
}