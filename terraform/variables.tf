variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "us-east-1"
}

variable "aws_az" {
  type        = string
  description = "AWS availability zone"
  default     = "us-east-1a"
}

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR"
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  type        = string
  description = "Public subnet CIDR"
  default     = "10.0.1.0/24"
}

variable "public_subnet_cidr_2" {
  type        = string
  description = "Second public subnet CIDR"
  default     = "10.0.2.0/24"
}

variable "private_subnet_cidr_1" {
  type        = string
  description = "First private subnet CIDR"
  default     = "10.0.11.0/24"
}

variable "private_subnet_cidr_2" {
  type        = string
  description = "Second private subnet CIDR"
  default     = "10.0.12.0/24"
}

variable "aws_az_2" {
  type        = string
  description = "Second AWS availability zone"
  default     = "us-east-1b"
}

variable "db_name" {
  type        = string
  description = "RDS database name"
  default     = "buildnest_ecommerce"
}

variable "db_username" {
  type        = string
  description = "RDS master username"
  default     = "buildnest_admin"
}

variable "db_password" {
  type        = string
  description = "RDS master password"
  sensitive   = true
}

variable "db_instance_class" {
  type        = string
  description = "RDS instance class"
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  type        = number
  description = "RDS allocated storage in GB"
  default     = 20
}

variable "redis_node_type" {
  type        = string
  description = "ElastiCache node type"
  default     = "cache.t3.micro"
}
