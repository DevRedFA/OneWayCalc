package ivan;

import ivan.XMLwrapper.*;

import javax.xml.bind.*;
import java.io.*;
import java.math.*;
import java.util.LinkedList;
import java.util.List;


import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ivan on 03.06.2017.
 */


public class Calc {
    public static final Logger logger = LoggerFactory.getLogger(Calc.class); //LoggerOne
    private static String log4jConfPath = "D:\\project\\OneWayCalc\\scr\\main\\resources\\log4j.properties";

    static {
        PropertyConfigurator.configure(log4jConfPath);
    }

    private LinkedList<BigDecimal> results = new LinkedList<>();
    private LinkedList<SimpleCalculator.ExpressionResults.ExpressionResult> resultsExpr = new LinkedList<>();
    private JAXBContext jc;
    private Unmarshaller u;
    private Marshaller m;

    public Calc() {
        try {
            logger.info("Creating JAXBContext");
            jc = JAXBContext.newInstance("ivan.XMLwrapper");
            u = jc.createUnmarshaller();
            m = jc.createMarshaller();
        } catch (JAXBException e) {
            logger.error("Error while creating JAXBContext", e);
        }
    }

    public void calculate(String[] args) {

        try {
            logger.info("Getting data from XML: {}", args[0]);
            SimpleCalculator calculator = getSimpleCalculator(args[0]);
            List<SimpleCalculator.Expressions.Expression> expressions = calculator.getExpressions().getExpression();
            for (SimpleCalculator.Expressions.Expression expression : expressions) {
                logger.info("Getting terms form {}", expression.toString());
                Term term = expression.getOperation();
                results.add(evaluate(term));
            }
        } catch (JAXBException e) {
            logger.error("file {} contains corrupted data: {}", args[0], e);
        } catch (FileNotFoundException e) {
            logger.error("File {} doesn't exist", args[0], e);
        }

        logger.info("Creating new Object factory");
        ObjectFactory factory = new ObjectFactory();
        SimpleCalculator calcResult = factory.createSimpleCalculator();
        SimpleCalculator.ExpressionResults calcResultExpressions = factory.createSimpleCalculatorExpressionResults();
        for (int i = 0; i < results.size(); i++) {
            logger.info("Adding result number {}", i);
            resultsExpr.add(factory.createSimpleCalculatorExpressionResultsExpressionResult());
            resultsExpr.get(i).setResult(results.get(i).doubleValue());
            calcResultExpressions.getExpressionResult().add(resultsExpr.get(i));
        }
        try {
            calcResult.setExpressionResults(calcResultExpressions);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            logger.info("Packing results to XML{}", System.lineSeparator());
            m.marshal(calcResult, new FileOutputStream(args[1]));
        } catch (JAXBException e) {
            logger.error("Errors while marshal to XML: {}", e);
        } catch (FileNotFoundException e) {
            logger.error("Cant create file for results {}", e);
        }

    }

    private SimpleCalculator getSimpleCalculator(String fileName) throws FileNotFoundException, JAXBException {
        return (SimpleCalculator) u.unmarshal(new FileInputStream(fileName));
    }


    public static void main(String[] args) {
        //        BasicConfigurator.configure();
        logger.info("-----------------------------------------------------");
        logger.info("Starting new run with params: {}, {}", args[0], args[1]);
        logger.info("-----------------------------------------------------{}", System.lineSeparator());
        if (args.length != 2) {
            logger.error("wrong args");
            return;
        }

        try {
            new Calc().calculate(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private BigDecimal evaluate(Term term) {
        LinkedList<BigDecimal> results = new LinkedList<>();
        List<Term> terms;
        List<BigInteger> args;
        logger.info("Starting evaluate {}", term.toString());
        args = term.getArg();

        if (args.size() != 0) {
            results.add(new BigDecimal(args.get(0)));
            results.add(new BigDecimal(args.get(1)));
        } else {
            logger.info("Starting counting args of {}", term.toString());
            terms = term.getOperation();
            if (term.getArg1() == null) {
                results.add(evaluate(terms.get(0)));
            }
            if (term.getArg2() == null) {
                results.add(evaluate(terms.get(1)));
            }
        }

        switch (term.getOperationType())

        {
            case "SUM":
                logger.info("counting {} SUM {}", results.get(0).toString(), results.get(1).toString());
                return results.get(0).add(results.get(1)).setScale(11, BigDecimal.ROUND_FLOOR);
            case "SUB":
                logger.info("counting {} SUB {}", results.get(0).toString(), results.get(1).toString());
                return results.get(0).subtract(results.get(1)).setScale(11, BigDecimal.ROUND_FLOOR);
            case "MUL":
                logger.info("counting {} MUL {}", results.get(0).toString(), results.get(1).toString());
                return results.get(0).multiply(results.get(1)).setScale(11, BigDecimal.ROUND_FLOOR);

            case "DIV":
                try {
                    logger.info("counting {} DIV {}", results.get(0).toString(), results.get(1).toString());
                    return results.get(0).divide(results.get(1), 11, BigDecimal.ROUND_FLOOR);
                } catch (ArithmeticException e) {
                    logger.error("divisor is zero", e);
                }
            default:
                logger.error("Wrong operation type: {}", term.getOperationType());
                return null;
        }
    }
}
