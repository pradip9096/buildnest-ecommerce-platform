resource "aws_elasticache_subnet_group" "buildnest" {
  name       = "buildnest-redis-subnet-group"
  subnet_ids = [aws_subnet.private_1.id, aws_subnet.private_2.id]

  tags = {
    Name = "buildnest-redis-subnet-group"
  }
}

resource "aws_elasticache_cluster" "buildnest" {
  cluster_id           = "buildnest-redis"
  engine               = "redis"
  node_type            = var.redis_node_type
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.buildnest.name
  security_group_ids   = [aws_security_group.redis_sg.id]

  tags = {
    Name = "buildnest-redis"
  }
}
