# Modified UPnP Library with Annotation

Original is

http://cgupnpjava.sourceforge.net/

## Before

repeating same code

```Java
public class LightDevice extends Device {

    private StateVariable powerVar;

    public LightDevice() throws IOException {
        super(LightDevice.class.getResource("light/description/description.xml"));

        Action getPowerAction = getAction("GetPower");
        getPowerAction.setActionListener(actionListener);

        Action setPowerAction = getAction("SetPower");
        setPowerAction.setActionListener(actionListener);

        List<Service> serviceList = getServiceList();
        Service service = serviceList.get(0);
        service.setQueryListener(queryListener);

        powerVar = getStateVariable("Power");

        Argument powerArg = getPowerAction.getArgument("Power");
        StateVariable powerState = powerArg.getRelatedStateVariable();
    }

    :

    private ActionListener actionListener = new ActionListener() {
        public boolean actionControlReceived(Action action) {
            String actionName = action.getName();
    
            boolean result = false;
    
            if (actionName.equals("GetPower")) {
                String state = getPowerState();
                Argument powerArg = action.getArgument("Power");
                powerArg.setValue(state);
                result = true;
            } else if (actionName.equals("SetPower")) {
                Argument powerArg = action.getArgument("Power");
                String state = powerArg.getValue();
                setPowerState(state);
                state = getPowerState();
    
                Argument resultArg = action.getArgument("Result");
                resultArg.setValue(state);
                result = true;
            }
    
            :
    
            return result;
        }
    };

    private QueryListener queryListener = new QueryListener() {
        public boolean queryControlReceived(StateVariable stateVar) {
            try {
                stateVar.setValue(getPowerState());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    };
}
```

## After

pojo w/ annotation

```Java
@UPnPDevice(description = "/light/description/description.xml")
public class Light {

    /** */
    @UPnPStateVariable(name = "Power")
    private int power = 0;

    @UPnPAction(name = "GetPower", arg = "Power")
    public int getPower() {
        return power;
    }

    @UPnPAction(name = "SetPower", arg = "Power", result = "Result")
    public void setPower(int power) {
        this.power = power;
        view.setPowerEnabled(power != 0);
    }

    :
}

    :

    public static void main(String[] args) throws Exception {

        Light model = new Light();
        final Device device = UPnPFactory.getDevice(model);

        :

        device.start();
```
