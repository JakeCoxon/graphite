require 'pp'

class String
  def camelize; self.gsub(/(\A|_)(\w)/) {$2.upcase}; end
  def xmlize; self.gsub(/_/, '-'); end
end
class Symbol
  def camelize; to_s.camelize; end
  def xmlize; to_s.xmlize; end
end

class Node

  attr_reader :full_name, :inline, :type, :params, :children, :root
  def initialize(type, parent, extra={}, &blck)
    @type = type = type.to_sym
    @children = {}
    @params = []
    @full_name = type
    @extra = extra
    if parent then
      @full_name = "#{parent.full_name}_#{type}"
      parent.children[type] = self

      @root = parent.root
    end
    root.all_nodes[full_name] = self
    instance_eval(&blck)
  end
  def [](i); @extra[i]; end

  def node(type, extra={}, &blck)
    Node.new(type, self, extra, &blck)
  end

  def required(argname, argtype, extra={})
    @params << {:name => argname, :type => argtype, :required => true}.merge(extra)
  end
  def optional(argname, argtype, extra={})
    @params << {:name => argname, :type => argtype, :required => false}.merge(extra)
  end
  def precode(str=nil); !str and return @precode; @precode = str; end
  def postcode(str=nil); !str and return @postcode; @postcode = str; end
  def code(str); postcode(str); end
  def children_of(nodename)
    @children = @root.all_nodes[nodename].children
  end

  def method_missing(name, *args, &blck)
    clsname = "#{name.camelize}Node"
    if Object.const_defined? clsname then
      type = args.shift
      return Object.const_get(clsname).new(type, self, *args, &blck)
    end

    super
  end

end
class RootNode < Node
  attr_reader :all_nodes
  def initialize(*args)
    @all_nodes = {}
    super
  end
  def root; self; end
end

def root(type, &blck)
  RootNode.new(type, nil, &blck)
end

class Printer

  def initialize()
    @indent = 0
    @output = ""
    @halt_tab = false
  end

  def println(str)
    if str == nil then return self end
    print(str); print("\n"); self
  end

  def <<(str); println(str); end

  def print(str)
    if str == :tab then @output << "  "
    elsif str != nil
      @output << tabs if @output[-1] == "\n"
      @output << str.gsub(/(?<=\n)(?!\Z)/, tabs)
    end; self
  end
  def to_s; @output; end

  protected

  def tabs(); "  " * @indent; end

  def indent(before=nil, after=nil)
    println before
    @indent += 1
    yield
    @indent -= 1
    println after
  end
end
