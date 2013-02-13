test do
  initial do
    vertex :v1, :v2, :v3, :_, :_, :_
    edge "A", :v1, :v2, :v3
  end

  rule :A do
    edge "B", :v1, :v2
  end

  output do
    vertex :v1, :v2, :v3, :_, :_, :_
    edge "B", :v1, :v2
  end
end