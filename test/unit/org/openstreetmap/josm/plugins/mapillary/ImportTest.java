package org.openstreetmap.josm.plugins.mapillary;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.mapillary.utils.MapillaryUtils;

/**
 * Test the importation of images.
 *
 * @author nokutu
 *
 */
public class ImportTest extends AbstractTest {

  /**
   * Test the importation of images in PNG format.
   */
  @Test
  public void importNoTagsTest() {
    File image = new File("images/icon16.png");
    MapillaryImportedImage img = MapillaryUtils.readNoTags(image,
        new LatLon(0, 0));
    assertEquals(0, img.getCa(), 0.01);
    assertTrue(new LatLon(0, 0).equalsEpsilon(img.getLatLon()));
  }

  /**
   * Test if provided an invalid file, the proper exception is thrown.
   *
   * @throws IOException
   */
  @Test(expected = IIOException.class)
  public void testInvalidFiles() throws IOException {
    MapillaryImportedImage img = new MapillaryImportedImage(0, 0, 0, null);
    assertEquals(null, img.getImage());
    assertEquals(null, img.getFile());

    img = new MapillaryImportedImage(0, 0, 0, new File(""));
    assertEquals(new File(""), img.getFile());
    img.getImage();
  }
}