import CalculatorApp.*; 
import org.omg.CORBA.*; 
import org.omg.CosNaming.*; 
 
public class CalculatorClient { 
  public static void main(String[] args) { 
    try { 
      // Initialize ORB 
      ORB orb = ORB.init(args, null); 
 
      // Get reference to naming service 
      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService"); 
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef); 
 
      // Lookup the Calculator object 
      Calculator calc = CalculatorHelper.narrow(ncRef.resolve_str("Calculator")); 
 
      // Invoke remote methods 
      System.out.println("5 + 3 = " + calc.add(5, 3)); 
      System.out.println("5 - 3 = " + calc.subtract(5, 3)); 
      System.out.println("5 * 3 = " + calc.multiply(5, 3)); 
      System.out.println("5 / 3 = " + calc.divide(5, 3)); 
    } catch (Exception e) { 
      e.printStackTrace(); 
    } 
  } 
} 