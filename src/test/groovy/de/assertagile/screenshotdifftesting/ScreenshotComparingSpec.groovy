package de.assertagile.screenshotdifftesting

import geb.waiting.Wait
import io.github.bonigarcia.wdm.WebDriverManager
import org.im4java.core.CompareCmd
import org.im4java.core.IMOperation
import org.im4java.process.StandardStream
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.chrome.ChromeDriver
import spock.lang.Specification

// based upon http://testandwin.net/test-automation/compare-screenshots/

class ScreenshotComparingSpec extends Specification {

    @Delegate
    static ChromeDriver driver

    @Delegate
    static Wait wait = new Wait()

    def setupSpec() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver()
    }

    def cleanupSpec() {
        driver.quit()
    }

    def "two different articles should differ"() {
        expect:
        !compareImages(
                captureScreenshot("http://assertagile.de/archives/238_toughts-about-the-purpose-of-testing-part-1/"),
                captureScreenshot("http://assertagile.de/archives/194_toughts-about-the-purpose-of-testing-part-2/")
        )
    }

    def "the same article should not differ"() {
        expect:
        compareImages(
                captureScreenshot("http://assertagile.de/archives/264_toughts-about-the-purpose-of-testing-part-3/"),
                captureScreenshot("http://assertagile.de/archives/264_toughts-about-the-purpose-of-testing-part-3/")
        )
    }

    private File captureScreenshot(final String url) {
        driver.get(url)
        return (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
    }

    private boolean compareImages(final File image1, final File image2) {
        String diffFilePath = "${specificationContext.currentSpec.name}-" +
                "${specificationContext.currentIteration.name}" +
                "-diff.png"

        // This instance wraps the compare command
        CompareCmd compare = new CompareCmd();

        // For metric-output
        compare.setErrorConsumer(StandardStream.STDERR);
        IMOperation cmpOp = new IMOperation();
        // Set the compare metric
        cmpOp.metric("mae");

        cmpOp.addImage(image1.absolutePath);
        cmpOp.addImage(image2.absolutePath);
        cmpOp.addImage(diffFilePath);

        try {
            // Do the compare
            compare.run(cmpOp);
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }
}
