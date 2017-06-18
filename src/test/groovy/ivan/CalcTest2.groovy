package ivan

import java.nio.file.Files
import java.nio.file.Paths

//import groovy.util.logging.Slf4j

/**
 * Created by Ivan on 13.06.2017.
 */

//@Slf4j
class CalcTest2 extends GroovyTestCase {
    void testMain() {
        List<String> countedResult
        List<String> correctResult
        String[] fileNames = ["D:\\project\\OneWayCalc\\sampleTest.xml", "D:\\project\\OneWayCalc\\ResultSampleTest.xml"]
        String resultFile = "D:\\project\\OneWayCalc\\CorrectResultSampleTest.xml"
        new Calc().main(fileNames)

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(resultFile))
            correctResult = new ArrayList<>()
            String s = reader.readLine()
            while (s != null) {
                correctResult.add(s)
                s = reader.readLine()
            }
        } catch (IOException e) {
            log.error("File with correct result {} doesn't exist", resultFile, e)
        }
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(fileNames[1]))
            countedResult = new ArrayList<>()
            String s = reader.readLine()
            while (s != null) {
                countedResult.add(s)
                s = reader.readLine()
            }
        } catch (IOException e) {
            log.error("Error while reading {} for check data", fileNames[1], e)
        }
        assertEquals(correctResult, countedResult)
    }
}