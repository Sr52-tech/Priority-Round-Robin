import java.util.Scanner;

public class PriorityRoundRobin {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Test case 1
        // int quantumTime = 2;
        // Process[] processes = { new Process(1, 0, 4, 3), new Process(2, 0, 5, 2), new
        // Process(3, 0, 8, 2),
        // new Process(4, 0, 7, 1), new Process(5, 0, 3, 3) };

        // Test case 2
        // int quantumTime = 4;
        // Process[] processes = { new Process(1, 0, 12, 3), new Process(2, 5, 19, 3),
        // new Process(3, 8, 21, 5),
        // new Process(4, 11, 13, 2), new Process(5, 15, 15, 3) };

        // Test case 3
        // int quantumTime = 10;
        // Process[] processes = { new Process(1, 0, 15, 3), new Process(2, 15, 25, 3),
        // new Process(3, 5, 5, 2),
        // new Process(4, 7, 5, 2), new Process(5, 25, 15, 1), new Process(6, 30, 5, 3),
        // };

        // Test case 4
        // int quantumTime = 1;
        // Process[] processes = { new Process(1, 0, 10, 3), new Process(2, 0, 1, 1),
        // new Process(3, 0, 2, 4),
        // new Process(4, 0, 1, 5), new Process(5, 0, 5, 2) };

        int quantumTime;
        do {
            System.out.print("Please enter the quantum time: ");
            quantumTime = input.nextInt();
            if (quantumTime < 0) {
                System.out.println("!!!!! Invalid input");
            }
        } while (quantumTime < 0);

        Process[] processes = new Process[0];
        // Get processes from the user
        int id;
        int arrivalTime;
        int burstTime;
        int priority;
        boolean exitFlag;
        do {
            System.out.println("Please enter process ID, arrival time, burst time, and priority for process number "
                    + (processes.length + 1));
            id = input.nextInt();
            arrivalTime = input.nextInt();
            burstTime = input.nextInt();
            priority = input.nextInt();
            exitFlag = id == 0 && arrivalTime == 0 && burstTime == 0 && priority == 0;
            if (exitFlag)
                break;
            // Process ID validation
            if (!isUnique(processes, id)) {
                System.out.println("!!!!! Process ID " + id + " is already reserved, try another one!");
                continue;
            }
            if (id < 0 || arrivalTime < 0 || burstTime < 0 || priority < 0) {
                System.out.println("!!!!! Invalid input");
                continue;
            }
            processes = pushProcess(processes, new Process(id, arrivalTime, burstTime, priority));
        } while (!exitFlag);

        // Sort processes by arrival time
        for (int i = 0; i < processes.length; i++) {
            int minIndex = i;
            for (int j = i + 1; j < processes.length; j++)
                if (processes[j].arrivalTime < processes[minIndex].arrivalTime)
                    minIndex = j;
            // Swap current index row with the min index row
            Process temp = processes[minIndex];
            processes[minIndex] = processes[i];
            processes[i] = temp;
        }

        // Calculate total burst time
        int totalBurst = 0;
        for (int i = 0; i < processes.length; i++)
            totalBurst += processes[i].burstTime;

        // Start conducting gantt chart
        Process[] newProcesses = new Process[processes.length];
        for (int i = 0; i < newProcesses.length; i++)
            newProcesses[i] = processes[i];
        int ganttChartCurrnetProcess = -1;
        Process[] waitingProcesses = new Process[0];
        Process[] terminatedProcesses = new Process[0];
        int currentPriority = 0;
        int currentProcessIndex;
        int priorityIndexArrayCounter = 0;
        int quantumTimer = quantumTime;
        for (int i = 0; i < totalBurst; i++) {

            for (int j = 0; j < newProcesses.length; j++) {
                if (newProcesses[j].arrivalTime == i)
                    waitingProcesses = pushProcess(waitingProcesses, newProcesses[j]);
            }

            if (quantumTimer == quantumTime)
                currentPriority = getLowestPriority(waitingProcesses);

            // Start with the process that have the lowest priority
            int[] priorityIndexArray = getProcessIndexByPriority(waitingProcesses,
                    currentPriority);

            if (priorityIndexArray.length >= 1) {
                if (priorityIndexArray.length == 1)
                    currentProcessIndex = priorityIndexArray[0];
                else
                    currentProcessIndex = priorityIndexArray[priorityIndexArrayCounter];

                if (waitingProcesses[currentProcessIndex].remainingTime > 0) {
                    // Gatt chart
                    if (ganttChartCurrnetProcess != currentProcessIndex) {
                        System.out.print(i + "-p" + waitingProcesses[currentProcessIndex].id + "-");
                        ganttChartCurrnetProcess = currentProcessIndex;
                    }
                    // Response time
                    if (waitingProcesses[currentProcessIndex].startTime == -1)
                        waitingProcesses[currentProcessIndex].startTime = i;
                    waitingProcesses[currentProcessIndex].remainingTime--;
                    quantumTimer--;
                }
                if (waitingProcesses[currentProcessIndex].remainingTime == 0) {
                    // Completion Time
                    waitingProcesses[currentProcessIndex].finishingTime = i + 1;
                    terminatedProcesses = pushProcess(terminatedProcesses, waitingProcesses[currentProcessIndex]);
                    waitingProcesses = removeProcess(waitingProcesses, currentProcessIndex);
                    ganttChartCurrnetProcess = -1;

                    if (priorityIndexArray.length == 1) {
                        currentPriority++;
                        quantumTimer = quantumTime;
                    } else {
                        priorityIndexArray = getProcessIndexByPriority(waitingProcesses, currentPriority);
                        if (priorityIndexArray.length == 1)
                            priorityIndexArrayCounter = 0;
                    }
                }
                if (quantumTimer == 0 && priorityIndexArray.length > 1) {
                    priorityIndexArrayCounter = (priorityIndexArrayCounter + 1) % priorityIndexArray.length;
                }

            }
            if (quantumTimer == 0) {
                quantumTimer = quantumTime;
            }
            if (i == totalBurst - 1)
                System.out.print(i + 1);
        }

        System.out.println("\nProcess ID" + "\t" + "Turnaround Time" + "\t" + "Response Time" + "\t" + "Waiting Time");
        // List processes
        for (int i = 0; i < terminatedProcesses.length; i++)
            processes[i].print();

        double turnaroundTimeSum = 0;
        double responseTimeSum = 0;
        double waitingTimeSum = 0;

        for (Process process : terminatedProcesses) {
            turnaroundTimeSum += process.finishingTime - process.arrivalTime;
            responseTimeSum += process.startTime - process.arrivalTime;
            waitingTimeSum += process.finishingTime - process.arrivalTime - process.burstTime;
        }

        double processesCount = terminatedProcesses.length;
        double averageTurnaroundTime = turnaroundTimeSum / processesCount;
        double averageResponseTime = responseTimeSum / processesCount;
        double averageWaitingTime = waitingTimeSum / processesCount;

        System.out.println("Average turnaround time: " + averageTurnaroundTime);
        System.out.println("Average response time: " + averageResponseTime);
        System.out.println("Average waiting time: " + averageWaitingTime);

        input.close();
    }

    static int[] getProcessIndexByPriority(Process[] array, int priority) {
        int[] priorityIndexArray = new int[0];
        for (int i = 0; i < array.length; i++)
            if (array[i].priority == priority)
                priorityIndexArray = pushElement(priorityIndexArray, i);
        return priorityIndexArray;
    }

    static int getLowestPriority(Process[] array) {
        int min = array[0].priority;
        for (int i = 1; i < array.length; i++)
            if (array[i].priority < min)
                min = array[i].priority;
        return min;
    }

    static int[] pushElement(int[] array, int newValue) {
        int[] longerArray = new int[array.length + 1];
        for (int i = 0; i < array.length; i++)
            longerArray[i] = array[i];
        longerArray[array.length] = newValue;
        return longerArray;
    }

    static Process[] pushProcess(Process[] array, Process newValues) {
        Process[] longerArray = new Process[array.length + 1];
        for (int i = 0; i < array.length; i++)
            longerArray[i] = array[i];
        longerArray[array.length] = newValues;
        return longerArray;
    }

    static Process[] removeProcess(Process[] array, int index) {
        Process[] updatedArray = new Process[array.length - 1];
        for (int i = 0, k = 0; i < array.length; i++) {
            if (i != index) {
                updatedArray[k] = array[i];
                k++;
            }
        }
        return updatedArray;
    }

    static boolean isUnique(Process[] processes, int value) {
        for (int i = 0; i < processes.length; i++)
            if (processes[i].id == value)
                return false;
        return true;
    }
}

class Process {
    int id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;
    int startTime;
    int finishingTime;

    Process(int id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        startTime = -1;
        finishingTime = -1;
    }

    public void print() {
        int turnaroundTime = this.finishingTime - this.arrivalTime;
        int responseTime = this.startTime - this.arrivalTime;
        int waitingTime = this.finishingTime - this.arrivalTime - this.burstTime;
        System.out.println(id + "\t\t" + turnaroundTime + "\t\t" + responseTime + "\t\t" + waitingTime);
    }
}
