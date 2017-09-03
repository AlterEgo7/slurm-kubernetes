require 'socket'
require 'httparty'
require 'date'

exit_requested = false
trap('INT') { exit_requested = true }

data = []
until exit_requested do

  before = Time.now

  slurm_stats = Thread.new do
    socket = TCPSocket.open('gluster1', 29705)
    begin
      stats = JSON.parse(socket.read)
    rescue JSON::ParserError
      stats = -1
    end
    socket.close
    Thread.current[:output] = stats
  end

  cluster_size = Thread.new do
    Thread.current[:output] = JSON.parse(HTTParty.get('http://gluster1:8001/apis/apps/v1beta1/namespaces/default/statefulsets/slurm').body).dig('status', 'readyReplicas')
  end

  [slurm_stats, cluster_size].each(&:join)

  if slurm_stats[:output] == -1
    data << [DateTime.now.to_s, data[-1][1], data[-1][2], cluster_size[:output]]
  else
    data << [DateTime.now.to_s, slurm_stats[:output]['queue_size'], slurm_stats[:output]['alloc_nodes'], cluster_size[:output]]
  end

  interval = 1 - (Time.now - before)
  sleep interval if interval > 0
end

CSV.open('/tmp/data.csv', 'wb') do |csv|
  csv << ['Time', 'Queue Size', 'Allocated Nodes', 'Cluster Size']
  data.each do |d|
    csv << d
  end
end