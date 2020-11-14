package com.aiad.agents;

import com.aiad.Config;
import com.aiad.messages.RunwayOperationCfp;
import com.aiad.messages.RunwayOperationProposal;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import javax.print.event.PrintJobEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import static com.aiad.Config.MAX_RUNWAY_CLEARANCE_TIME;
import static com.aiad.Config.TICKRATE;

public class Runway extends Agent {
    final public static double DEBRIS_APPEARANCE_PROBABILITY = 0.3d;
    final private Random rand = new Random();
    final private TreeMap<Integer, Operation> operations = new TreeMap<>();
    int id;
    boolean isClear = true;
    private RunwayClearingBehaviour _clearingBehaviour;
    private RunwayUpdatingBehaviour _updatingBehaviour;
    protected JFrame frame;
    protected JTable table;

    public Runway(int id, JFrame frame ) {
        this.id = id;
        this.frame = frame;

              JPanel panel = new JPanel();
        panel.setBackground(Color.GRAY);
        panel.setLayout(new FlowLayout());

        table = new JTable(1,200);
        table.setBounds(10,170, 600, 50);
        table.setRowHeight(20);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < table.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(20);
        }

        JLabel label = new JLabel("runway" + id);
        label.setBounds(10,10 + (300*(id-1)),800,40);

        panel.add(label);
        panel.add(table);
        frame.add(panel);
        frame.getContentPane().add( new JScrollPane( panel ), BorderLayout.CENTER );
        frame.setVisible(true);

    }

    public Runway(String message) {
        String[] splitMessage = message.split(" ");
        this.id = Integer.parseInt(splitMessage[0]);
        this.isClear = splitMessage[1].equals("true");
    }

    int getEarliestSlot(int minTime) {
        var followingOperations = operations.tailMap(minTime);
        var time = isClear ? minTime : Math.max(minTime, _clearingBehaviour.clearanceTime);
        for (var operation : followingOperations.entrySet()) {
            if (minTime + Config.OPERATION_LENGTH <= operation.getKey())
                break;
            else
                time = operation.getKey() + Config.OPERATION_LENGTH;
        }
        return time;
    }

    void updateOperations() {
        // deal with 0 value
        var willBeObstructed = false;
        if (operations.containsKey(0)) {
            operations.get(0).tick();
            if (operations.get(0).getDuration() == 0) {
                operations.remove(0);
                willBeObstructed = willBecomeObstructed();
            }
        }
        // decrement time to next operations
        var keys = new ArrayList<>(operations.tailMap(0, false).keySet());
        for (var key : keys) {
            var operation = operations.get(key);
            operations.remove(key);
            operations.put(key - 1, operation);
        }

        if (willBeObstructed)
            setObstructed();
    }


    protected void setup() {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(getAID());
        ServiceDescription service = new ServiceDescription();
        service.setName("runway");
        service.setType("runway");

        setupUpdatingBehaviour();

        addBehaviour(new ProposalBuilder(this, new MessageTemplate(
                (MessageTemplate.MatchExpression) msg -> msg.getPerformative() == ACLMessage.CFP)
        ));

        description.addServices(service);

        try {
            DFService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    /*
     *   The message will be the following :
     *   " id isClear"
     *
     */

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    public boolean isClear() {
        return this.isClear;
    }

    public boolean willBecomeObstructed() {
        return rand.nextDouble() < DEBRIS_APPEARANCE_PROBABILITY;
    }

    public void setObstructed() {
        var clearanceTime = rand.nextInt(MAX_RUNWAY_CLEARANCE_TIME);
        setupClearingBehaviour(clearanceTime);
        isClear = false;
        var affectedKeys = new ArrayList<>(operations.headMap(clearanceTime).keySet());
        for (var key : affectedKeys) {
            operations.computeIfPresent(key, (k, operation) -> {
                var msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Cancelled");
                msg.addReceiver(new AID("airplane" + operation.getAirplaneId(), AID.ISLOCALNAME));
                send(msg);
                return null;
            });
        }
    }

    private void setupClearingBehaviour(int clearanceTime) {
        _clearingBehaviour = new RunwayClearingBehaviour(this, TICKRATE, clearanceTime);
        addBehaviour(_clearingBehaviour);
    }

    private void setupUpdatingBehaviour() {
        _updatingBehaviour = new RunwayUpdatingBehaviour(this, TICKRATE);
        _updatingBehaviour.setFixedPeriod(true);
        addBehaviour(_updatingBehaviour);
    }

    public void setClear() {
        isClear = true;
        _clearingBehaviour = null;
    }

    public static class Operation implements Serializable {
        private final int airplaneId;
        private int duration;

        public Operation(int airplaneId, int duration) {
            this.airplaneId = airplaneId;
            this.duration = duration;
        }

        public int getAirplaneId() {
            return airplaneId;
        }

        public int getDuration() {
            return duration;
        }

        public void tick() {
            duration--;
        }
    }

    public static class RunwayClearingBehaviour extends TickerBehaviour {
        final private int clearanceTime;

        public int getClearanceTime() {
            return clearanceTime;
        }

        public RunwayClearingBehaviour(Runway runway, int tickRate, int clearanceTime) {
            super(runway, 1000 / tickRate);
            this.clearanceTime = clearanceTime;
        }

        @Override
        protected void onTick() {
            if (getTickCount() == clearanceTime) {
                ((Runway) this.getAgent()).setClear();
            }
            this.stop();
        }
    }

    public static class RunwayUpdatingBehaviour extends TickerBehaviour {
        public RunwayUpdatingBehaviour(Runway runway, int tickRate) {
            super(runway, 1000 / tickRate);
        }

        @Override
        protected void onTick() {
            var runway = (Runway) this.getAgent();
            runway.updateOperations();
            TreeMap<Integer, Operation> treeMap = runway.getOperations();
            DefaultTableModel model = (DefaultTableModel) runway.table.getModel();

            for(int i=0; i<runway.table.getColumnCount();i++){
                model.setValueAt(" ",0 , i);
            }
            for(var entry:treeMap.entrySet()){

                for(int i=entry.getKey(); i<runway.table.getColumnCount() && i<entry.getValue().duration + entry.getKey();i++){
                   model.setValueAt(entry.getValue().getAirplaneId(),0 , i);
                }

            }

        }
    }

    public TreeMap<Integer, Operation> getOperations() {
        return operations;
    }

    private static class ProposalBuilder extends ContractNetResponder {

        public ProposalBuilder(Agent agent, MessageTemplate mt) {
            super(agent, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException {
            var runway = (Runway) getAgent();

            System.out.println("Received CFP");

            // evaluate possibilities and make the best proposal possible
            try {
                var call = (RunwayOperationCfp) cfp.getContentObject();
                var slot = runway.getEarliestSlot(call.getMinTime());

                runway._updatingBehaviour.reset();

                var proposal = RunwayOperationProposal.build(runway.getId(), slot);
                var reply = cfp.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContentObject(proposal);
                System.out.println("RUNWAY :: " + myAgent.getLocalName() + " :: Sent proposal.");

                return reply;
            } catch (Exception e) {
                e.printStackTrace();
                throw new NotUnderstoodException(e.toString());
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println("RUNWAY :: " + myAgent.getLocalName() + " :: Received an accept.");
            var runway = (Runway) getAgent();
            try {
                // schedule the operation
                var airplaneId = ((RunwayOperationCfp) cfp.getContentObject()).getAirplaneId();
                var agreedOperationTime = ((RunwayOperationProposal) propose.getContentObject()).getOperationTime();
                var actualOperationTime = agreedOperationTime - runway._updatingBehaviour.getTickCount();

                var operation = new Operation(airplaneId, Config.OPERATION_LENGTH);

                runway.operations.put(actualOperationTime, operation);

                // send scheduled operation
                ACLMessage result = accept.createReply();
                result.setPerformative(ACLMessage.INFORM);
                result.setContentObject(operation);

                return result;
            } catch (Exception e) {
                throw new FailureException(e.toString());
            }
        }
    }
}
