close all;
load;

PendingQueueSize = QueueSize;
QueueSize = AllocatedNodes + PendingQueueSize;

lineWidth = 1.5;

plot(t, AllocatedNodes, 'LineWidth', lineWidth, 'Color', [0.4660 0.6740 0.1880], 'DisplayName', 'Allocated Nodes');
hold on;
plot(t, ClusterSize, 'LineWidth', lineWidth, 'DisplayName', 'Cluster Size', 'Color', [0.8500 0.3250 0.0980]);
plot(t, QueueSize, 'LineWidth', lineWidth, 'DisplayName', 'Queue Size', 'Color', [0 0.4470 0.7410]);

leg = legend('show');
leg.FontSize = 15;
xlabel('Time (sec)', 'FontSize', 15);
ylabel('Items', 'FontSize', 15);

hold off;

figure();

plot(t, AllocatedNodes, 'LineWidth', lineWidth, 'Color', [0.4660 0.6740 0.1880], 'DisplayName', 'Allocated Nodes');
hold on;
plot(t, ClusterSize, 'LineWidth', lineWidth, 'DisplayName', 'Cluster Size', 'Color', [0.8500 0.3250 0.0980]);
plot(t, QueueSize, 'LineWidth', lineWidth, 'DisplayName', 'Queue Size', 'Color', [0 0.4470 0.7410]);


axis([0 275 0 12]);
xlabel('Time (sec)', 'FontSize', 15);
ylabel('Items', 'FontSize', 15);
leg = legend('show');
leg.FontSize = 15;
