output "vpc_id" {
  value = aws_vpc.buildnest.id
}

output "public_subnet_id" {
  value = aws_subnet.public.id
}

output "public_subnet_id_2" {
  value = aws_subnet.public_2.id
}

output "private_subnet_id_1" {
  value = aws_subnet.private_1.id
}

output "private_subnet_id_2" {
  value = aws_subnet.private_2.id
}

output "app_security_group_id" {
  value = aws_security_group.app_sg.id
}

output "alb_dns_name" {
  value = aws_lb.buildnest.dns_name
}

output "rds_endpoint" {
  value = aws_db_instance.buildnest.endpoint
}

output "redis_endpoint" {
  value = aws_elasticache_cluster.buildnest.cache_nodes[0].address
}
